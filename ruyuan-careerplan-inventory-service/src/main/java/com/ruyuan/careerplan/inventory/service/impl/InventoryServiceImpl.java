package com.ruyuan.careerplan.inventory.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.inventory.Jedis.CacheSupport;
import com.ruyuan.careerplan.inventory.constants.RedisKeyConstants;
import com.ruyuan.careerplan.inventory.constants.RedisLua;
import com.ruyuan.careerplan.inventory.constants.RocketMqConstant;
import com.ruyuan.careerplan.inventory.constants.StockBucket;
import com.ruyuan.careerplan.inventory.converter.InventoryConverter;
import com.ruyuan.careerplan.inventory.dao.InventoryDAO;
import com.ruyuan.careerplan.inventory.dao.StorageDetailLogDAO;
import com.ruyuan.careerplan.inventory.dao.StorageInfoDAO;
import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
import com.ruyuan.careerplan.inventory.domain.entity.StorageDetailLogDO;
import com.ruyuan.careerplan.inventory.domain.entity.StorageInfoDO;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import com.ruyuan.careerplan.inventory.exception.InventoryBizException;
import com.ruyuan.careerplan.inventory.mq.producer.DefaultProducer;
import com.ruyuan.careerplan.inventory.service.InventoryService;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;


/**
 *
 * 库存服务业务类
 * @author zhonghuashishan
 */
@Service
public class InventoryServiceImpl implements InventoryService {

	private static Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

	@Autowired
	private CacheSupport cacheSupport;

	@Autowired
	private InventoryDAO inventoryDAO;

	@Autowired
	private StorageInfoDAO storageInfoDAO;

	@Autowired
	private StorageDetailLogDAO storageDetailLogDAO;
	@Autowired
	private DefaultProducer defaultProducer;
	@Resource
	private InventoryConverter inventoryConverter;

	/**
	 * 商品库存入库
	 * @param request
	 */
	@Override
	public void putStorage(InventoryRequest request) {
		//1.异步更新数据DB
		sendAsyncStockUpdateMessage(request);
		//2.执行库存均匀分发
		executeStockLua(request);
	}

	/**
	 * 扣减，返还商品库存
	 * @param request
	 */
	@Override
	public void deductProductStock(InventoryRequest request) {
		//1. 维护一个商品的消费购买次数Key，每次自增+1,并返回本次的请求次数
		Integer incrementCount = increment(request);
		//2.检测分桶内的库存是否足够购买，如果不够，则选择从新的分桶(库存最大的)进行合并后扣除。
		deductStockLua(incrementCount,request.getSkuId(),request.getInventoryNum());
		//3. 对库存的变化，进行异步落库变更(使用MQ进行保证数据最终一致性)
		//注意这里是扣除库存，和入库的是反向的，所以这里要正数转负数，负数转正数变更具体的库存数据
		request.setInventoryNum(0-request.getInventoryNum());
		sendAsyncStockUpdateMessage(request);
	}


	/**
	 *  查询当前商品的剩余库存
	 * @param skuId 入参查询条件
	 * @return 商品数量
	 */
	@Override
	public BigDecimal queryProductStock(Long skuId) {
		//1. 遍历redis
		BigDecimal productNum = BigDecimal.ZERO;
		String productStockKey = RedisKeyConstants.PRODUCT_STOCK_PREFIX + skuId;

		int redisCount = cacheSupport.getRedisCount();
		for (long i = 0;i<redisCount;i++){
			Object eval = cacheSupport.eval(i, RedisLua.QUERY_STOCK,CollUtil.toList(productStockKey),CollUtil.toList(productStockKey));
			if (!Objects.isNull(eval)){
				productNum = productNum.add(BigDecimal.valueOf(Long.valueOf(eval+"")));
			}
		}
		return productNum;
	}

	/**
	 * 库存变更操作
	 * @param request
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateInventory(InventoryRequest request){
		// 操作库存变更
		InventoryDO inventoryDO = saveStorageStock(request);
		// 记录变更明细
		saveStorageDetailLog(inventoryDO,request);
	}

	/**
	 * 执行库存分配,使用lua脚本执行库存的变更
	 * @param request 变更库存对象
	 */
	@Override
	public void executeStockLua(InventoryRequest request) {
		String productStockKey = RedisKeyConstants.PRODUCT_STOCK_PREFIX  + request.getSkuId();
		Integer sumNum = 0;
		Long startTime = System.currentTimeMillis();
		try {
			// 获取默认设定分桶
			int redisCount = cacheSupport.getRedisCount();
			// 先进行库存分配预估
			Integer inventoryNum = request.getInventoryNum();
			// 单个机器预计分配的库存
			Integer countNum = inventoryNum / redisCount;
			// 这里分为2个维度，一个维度十分之一的数量维度，一个维度是默认3个数量一次
			countNum = getAverageStockNum(countNum,redisCount);
			int i=0;
			while (true){
				for (long count=0;count <redisCount;count++ ){
					// 最后一次分配的库存小于预计分配库存的时候，则以剩余的库存为准
					if (inventoryNum - sumNum < countNum){
						countNum = inventoryNum - sumNum;
					}
					Object eval = cacheSupport.eval(count, RedisLua.ADD_INVENTORY, CollUtil.toList(productStockKey), CollUtil.toList(String.valueOf(countNum)));
					if (!Objects.isNull(eval) && Long.valueOf(eval+"") > 0){
						// 分配成功的才累计(可能出现不均匀的情况)
						sumNum = sumNum + countNum;
						i++;
					}

					if (sumNum.equals(inventoryNum)){
						break;
					}
				}
				//分配完成跳出循环
				if (sumNum.equals(inventoryNum)){
					break;
				}
			}
			log.info("商品编号："+request.getSkuId()+"，同步分配库存共分配"+ (i)+"次"+"，分配库存："+sumNum+",总计耗时"+(System.currentTimeMillis() - startTime)+"毫秒");
		}catch (Exception e){
			e.printStackTrace();
			// 同步过程中发生异常，去掉已被同步的缓存库存，发送消息再行补偿,这里出现异常不抛出，避免异常
			request.setInventoryNum(request.getInventoryNum() - sumNum);
			sendAsyncStockCompensationMessage(request);
			log.error("分配库存到缓存过程中失败", e.getMessage(), e);
		}
	}

	/**
	 * 获取每个机器预估的分配库存数量
	 * @param countNum
	 * @return
	 */
	private Integer getAverageStockNum(Integer countNum,Integer redisCount){
		Integer num = 0;
		//1. 首先如果总库存数量 平均分配到单个分配的执行次数不超过10次，则默认每次分配3个，如果超过10次，则以 预估分配数量/10 得到实际的每次分配数量
		if (countNum > (redisCount * StockBucket.STOCK_COUNT_NUM)){
			num = countNum / StockBucket.STOCK_COUNT_NUM;
		} else if (countNum > 3){
			num = 3;
		} else {
			num = countNum;
		}
		return num;
	}
	/**
	 * 记录本次出入库的明细记录
	 * @param inventoryDO
	 * @param request
	 */
	private void saveStorageDetailLog(InventoryDO inventoryDO, InventoryRequest request) {
		// 记录本次入库记录
		StorageInfoDO storageInfoDO = inventoryConverter.converterStorageRequest(request);
		storageInfoDO.setStorageTime(new DateTime());
		storageInfoDAO.save(storageInfoDO);
		// 记录本次记录的明细
		StorageDetailLogDO storageDetailLogDO = inventoryConverter.converterStorageLogRequest(inventoryDO);
		storageDetailLogDO.setStorageBeforeNum(inventoryDO.getInventoryNum() - request.getInventoryNum());
		storageDetailLogDO.setStorageCode(request.getWarehouseCode());
		storageDetailLogDO.setStorageNum(request.getInventoryNum());
		storageDetailLogDO.setStorageTime(new DateTime());
		storageDetailLogDAO.save(storageDetailLogDO);
	}
    /**
	 * 对指定的分桶进行数据扣减或者返回库存的lua脚本执行
	 * @param incrementCount 分桶标识
	 * @param skuId 商品ID
	 * @param stockNum 扣减或者返回库存
	 */
	private void deductStockLua(Integer incrementCount, Long skuId, Integer stockNum) {
		String productStockKey = RedisKeyConstants.PRODUCT_STOCK_PREFIX  + skuId;
		int redisCount = cacheSupport.getRedisCount();
		long maxSequence = incrementCount + redisCount - 1;
		Object result;
		Boolean deduct = false;
		Long startTime = System.currentTimeMillis();
		// 当一个桶不足以扣除，循环至下一个桶进行扣除，直到全部不够后，进行合并处理
		for (long i = incrementCount; i <= maxSequence; i++) {
			result = cacheSupport.eval(i, RedisLua.SCRIPT, CollUtil.toList(productStockKey), CollUtil.toList(String.valueOf(stockNum)));
			if (Objects.isNull(result)) {
				continue;
			}
			if (Integer.valueOf(result+"") > 0){
				int index = (int)(i % redisCount);
				log.info("redis实例[{}] 商品[{}] 本次扣减缓存库存:[{}], 剩余缓存库存:[{}],耗时：[{}]", index,skuId, stockNum, result,System.currentTimeMillis() - startTime);
				deduct = true;
				break;
			}
		}
		// 单个分片已经无法扣减库存了，进行合并扣除
		if (!deduct){
			// 获取一下当前的商品总库存，如果总库存也已不足以扣减则直接失败
			BigDecimal sumNum = queryProductStock(skuId);
			if (sumNum.compareTo(new BigDecimal(stockNum)) >=0 ){
				mergeDeductStock(productStockKey,stockNum);
			}
			throw new InventoryBizException("库存不足");
		}
	}

	/**
	 * 扣除某个桶的库存不足的时候，进行合并扣减
	 * @param productStockKey 缓存Key
	 * @param stockNum 扣减或者返回库存
	 * @return 合并的缓存key集合
	 */
	private void mergeDeductStock(String productStockKey, Integer stockNum){
		//1.执行多个分片的扣除扣减，对该商品的库存操作上锁，保证原子性
        Map<Long,Integer> fallbackMap = new HashMap<>();
		int redisCount = cacheSupport.getRedisCount();
		try {
			// 开始循环扣减库存
			for (long i=0;i<redisCount;i++){
				if (stockNum > 0){
					Object diffNum = cacheSupport.eval(i, RedisLua.MERGE_SCRIPT, CollUtil.toList(productStockKey), CollUtil.toList(stockNum + ""));
					if (Objects.isNull(diffNum)){
						continue;
					}

					// 当扣减后返回得值大于0的时候，说明还有库存未能被扣减，对下一个分片进行扣减
					if (Integer.valueOf(diffNum+"") >= 0){
						// 存储每一次扣减的记录，防止最终扣减还是失败进行回滚
						fallbackMap.put(i, (stockNum - Integer.valueOf(diffNum+"")));
						// 重置抵扣后的库存
						stockNum = Integer.valueOf(diffNum+"");

					}
				}
			}
			// 完全扣除所有的分片库存后，还是未清零，则回退库存返回各自分区
			if (stockNum > 0){
				fallbackMap.forEach((k, v) -> {
					Object result = cacheSupport.eval(k, RedisLua.SCRIPT, CollUtil.toList(productStockKey), CollUtil.toList((0 - v) + ""));
					log.info("redis实例[{}] 商品[{}] 本次库存不足，扣减失败，返还缓存库存:[{}], 剩余缓存库存:[{}]", k,productStockKey, v, result);
				});
				throw new InventoryBizException("库存不足");
			}

		}catch (Exception e){
			e.printStackTrace();
			// 开始循环返还库存
			fallbackMap.forEach((k, v) -> {
				cacheSupport.eval(k, RedisLua.SCRIPT,CollUtil.toList(productStockKey),CollUtil.toList((0-v)+""));
			});
			throw new InventoryBizException("合并扣除库存过程中发送异常");
		}
	}

	/**
	 * 每次请求，商品扣减库存自增1，用于分桶取模
	 * @param request
	 */
	private Integer increment(InventoryRequest request) {
		Long startTime = System.currentTimeMillis();
		String incrementKey = RedisKeyConstants.PRODUCT_STOCK_COUNT_PREFIX + request.getSkuId();

		Long incrementCount = cacheSupport.incr(incrementKey);
		log.info("商品编号："+request.getSkuId()+"获取访问次数"+incrementCount+",总计耗时"+(System.currentTimeMillis() - startTime)+"毫秒");
		return  incrementCount.intValue();
	}


	/**
	 * 保存商品的入库信息
	 * @param request
	 */
	private InventoryDO saveStorageStock(InventoryRequest request) {
		LambdaQueryWrapper<InventoryDO> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(InventoryDO::getSkuId, request.getSkuId());

		InventoryDO inventoryDO = inventoryDAO.getOne(queryWrapper);
		//还没有这个商品的入库信息
		if (Objects.isNull(inventoryDO)){
			inventoryDO = inventoryConverter.converterRequest(request);
            inventoryDO.setCreateTime(new Date());
            inventoryDO.setUpdateTime(new Date());
			inventoryDAO.save(inventoryDO);
		} else {
			inventoryDO.setInventoryNum(inventoryDO.getInventoryNum()+request.getInventoryNum());
            inventoryDO.setUpdateTime(new Date());
			inventoryDAO.updateById(inventoryDO);
		}
		return inventoryDO;
	}

	/**
	 * 库存缓存补偿的消息发送
	 *
	 * @param request
	 */
	private void sendAsyncStockCompensationMessage(InventoryRequest request) {
		// 发送消息到MQ
		log.info("库存缓存补偿消息发送到MQ, topic: {}, InventoryRequest: {}", RocketMqConstant.COMPENSATION_PRODUCT_STOCK_TOPIC, JsonUtil.object2Json(request));
		defaultProducer.sendMessage(RocketMqConstant.COMPENSATION_PRODUCT_STOCK_TOPIC,
				JsonUtil.object2Json(request), "COOKBOOK库存缓存补偿消息");
	}
	/**
	 * 库存变更的消息发送
	 *
	 * @param request
	 */
	private void sendAsyncStockUpdateMessage(InventoryRequest request) {
		Long startTime = System.currentTimeMillis();
		// 发送消息到MQ
		defaultProducer.sendMessage(RocketMqConstant.INVENTORY_PRODUCT_STOCK_TOPIC,
				JsonUtil.object2Json(request), "COOKBOOK库存变更异步落库消息");
		log.info("商品编号："+request.getSkuId()+"发送mq,总计耗时"+(System.currentTimeMillis() - startTime)+"毫秒");
	}
}
