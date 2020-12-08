package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.MyGmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yuanpf
 * @create 2020-11-30 11:10
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override//①查询一级分类信息
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    //select * from base_category2 where category1_id = category1Id
    @Override//②根据以及分类id查询二级分类信息
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id",category1Id));
    }

    //select * from base_category3 where category2_id = category2Id
    @Override//③根据二级id查询三级分类的信息
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }

    /**
     * ④根据分类id查询平台属性【单表】
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     *
     * select * from base_attr_info where category_id=? and category_level=?
     */
    /**
     * 既需要查询平台属性也需要查询平台属性值
     * 如何做多表查询？
     * 第一步：先找到要查询的表 根据查询的字段确认
     * 第二步：根据业务找到表和表的关系，left join  \   inner join  \ full join()
     * 第三步：看是否有过滤条件  category1Id
     * 第四步：根据需要的业务，再次调整，写出最终的sql
     */
    @Override //④根据分类id查询平台属性及平台属性值
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //select * from base_attr_info bai  inner join base_attr_value  bav
        //on bai.id = bav.attr_id where bai.category_id=? and bai.category_level=?
       List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
        return baseAttrInfoList;
    }

    //保存或修改平台属性和值
    @Override
    @Transactional//添加事务，保证一个添加成功，全部添加成功，全部添加成功，一个添加失败，全部添加失败
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() == null){//id为空，添加
            //第一步：保存平台属性
            baseAttrInfoMapper.insert(baseAttrInfo);
            //int a = 10/0;//测试事务
        }else {//修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }
        //先删除平台属性值，然后再添加

        baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>().eq("attr_id",baseAttrInfo.getId()));

        //第二步：保存平台属性值的集合
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);//添加属性值
            }
        }
    }

    //根据id获取平台属性的值select * from base_attr_info bai where bai.id = ?
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {

        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id",attrId));
    }
    /**
     * 根据平台属性id获取平台属性对象
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null){
            //获取平台属性值集合，将属性值集合放入该对象
            baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoPageList(Page<SpuInfo> page1, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",spuInfo.getCategory3Id());
        wrapper.orderByDesc("id");
        IPage<SpuInfo> spuInfoPageList = spuInfoMapper.selectPage(page1,wrapper);
        return spuInfoPageList;
    }

    @Override
    public IPage getBaseTrademarkPage(Page<BaseTrademark> baseTrademarkPage) {
        QueryWrapper<BaseTrademark> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        return  baseTrademarkMapper.selectPage(baseTrademarkPage,wrapper);
    }

    /**
     * 根据skuid获取商品基本属性和图片
     * @param skuId
     * @return
     */
    //使用redissin实现分布式锁
    @Override
    @MyGmallCache(prefix = "skuInfo")
    public SkuInfo getSkuInfoBySkuId(Long skuId) {
        return getSkuInfoDB(skuId);
    }


    private SkuInfo getSkuInfoByRedission(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            //定义数据在缓存中的key
            String skuInfoKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //根据key获取数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuInfoKey);
            if (skuInfo == null){//缓存中没有数据，需要查询数据库，添加到缓存中，
                //采用分布式锁，避免缓存穿透
                //定义分布式锁的key
                String distributedKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                //获取锁的对象
                RLock rLock = redissonClient.getLock(distributedKey);
                //上锁
                boolean flag = rLock.tryLock(RedisConst.SKULOCK_EXPIRE_PX2, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (flag){//上锁成功
                    try {
                        skuInfo = getSkuInfoDB(skuId);
                        if (skuInfo==null){//数据库中不存在该记录，返回一个空的对象放入缓存并设置过期时间，不能太长
                            SkuInfo skuInfo1 = new SkuInfo();
                            //放入缓存
                            redisTemplate.opsForValue().set(skuInfoKey,
                                                            skuInfo1,
                                                            RedisConst.SKUKEY_TEMPORARY_TIMEOUT,
                                                            TimeUnit.SECONDS);
                            return skuInfo1;
                        }else {
                            redisTemplate.opsForValue().set(skuInfoKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo;//查询到数据
                        }

                    }finally {
                        rLock.unlock();
                    }
                }else {//上锁失败
                    try {
                        //没有抢到锁的线程，等待，自旋，等待再次查询缓存-》数据库-》缓存-》数据库
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //自旋
                    return getSkuInfoBySkuId(skuId);
                }
            }else {
                return skuInfo;//缓存中有数据
            }
        } catch (InterruptedException e) {
            //通知运维进行维护等
            e.printStackTrace();
        }
        return getSkuInfoDB(skuId);//发生异常直接数据库兜底
    }

    //使用redis的set命令整合reids缓存，使用分布式锁
    private SkuInfo getSkuInfoByRedis(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            //先获取缓存中的数据
            //先获取缓存中的key,    先选择缓存中存放数据的key ->  定义存放数据的key
            String skuInfoKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //根据key从缓从中获取数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuInfoKey);
            //如果缓存中有从缓存中取，如果没有则从数据库获取并写入缓存当中
            if (skuInfo == null){//缓存中没有数据
                //采用分布式锁，避免缓存击穿
                    //定义分布式锁的key
                String distributedKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;

                //声明一个uuid
                String uuid = UUID.randomUUID().toString();

                //上锁
                Boolean flag = redisTemplate.opsForValue()
                                            .setIfAbsent(distributedKey,
                                                         uuid,
                                                         RedisConst.SKULOCK_EXPIRE_PX1,
                                                         TimeUnit.SECONDS);
                if (flag){//上锁成功，从数据库获取数据并写入缓存
                    skuInfo = getSkuInfoDB(skuId);
                    //如果数据为空，会发生缓存穿透，所以需要添加分布式锁
                    if (skuInfo == null){
                        SkuInfo skuInfo1 = new SkuInfo();
                        redisTemplate.opsForValue().set(skuInfoKey,skuInfo,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);//key,value,过期时间，单位
                        //不需要删除锁
                        return skuInfo1;
                    }
                    //将数据放入缓存
                    redisTemplate.opsForValue().set(skuInfoKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    //删除锁
                    //定义lua脚本
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptText(script);
                    redisScript.setResultType(Long.class);
                    //删除
                    redisTemplate.execute(redisScript, Arrays.asList(distributedKey),uuid);
                    return skuInfo;
                }else{//上锁失败
                    try {
                        //等待自旋
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //自旋
                    return getSkuInfoBySkuId(skuId);
                }
            }else {//缓存中有数据
                return skuInfo;
            }
        } catch (Exception e) {
            //发生异常，记录日志，通知运维进行维护，发送短信等方式
            e.printStackTrace();
        }
        //如果发生异常，先使用数据库兜底
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        //根据skuid获取商品基本属性
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null){//如果skuinfo为空，会发生空指针
            //根据skuid获取商品的图片信息
            List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    /**
     * 需求分析：
     * 三级分类id。在skuinfo中提供
     * 根据三级分类id获取三级分类名称、二级分类id
     * 根据二级分类id获取二级分类名称、一级分类id
     * 根据一级分类id获取一级分类名称
     *具体实现：
     * 通过三级分类id查询一级分类、二级分类、三级分类的视图获取数据
     */

    @Override
    @MyGmallCache(prefix = "baseCategoryName")
    public BaseCategoryView getBaseCategoryName(Long category3Id) {

        return baseCategoryViewMapper.selectById(category3Id);
    }
    /**
     * 根据skuid获取最新的价格
     */
    @Override
    @MyGmallCache(prefix = "skuPrice")
    public BigDecimal getSkuPriceBySkuId(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null){
            return skuInfo.getPrice();
        }
       return new BigDecimal(0);
    }

    //获取首页显示的分类数据的方法
    @Override
    @MyGmallCache(prefix = "baseCategoryList")
    public List<JSONObject> getBaseCategoryList() {
        List<JSONObject> list = new ArrayList<>();
        //查询视图得到所有的数据【一级分类名称，id,二级分类名称，id ,三级分类的名称，id
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        //得到所有的一级分类的id
        Map<Long, List<BaseCategoryView>> baseCategory1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //遍历
        int index = 1;
        for (Map.Entry<Long, List<BaseCategoryView>> category1List  : baseCategory1Map.entrySet()) {
            //生命一个对象，存放所有的一级分类
            JSONObject categoryView1 = new JSONObject();

            Long category1Id = category1List.getKey();
            List<BaseCategoryView> category2List = category1List.getValue();
            categoryView1.put("index",index);
            categoryView1.put("categoryId",category1Id);
            //一级分类下的二级分类的一级分类的名称和一级分类的名称一样
            categoryView1.put("categoryName",category2List.get(0).getCategory1Name());
            index++;

            //获取所有的二级分类的数据
            List<JSONObject> category1Child = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> baseCategory2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> category2collect : baseCategory2Map.entrySet()) {
                JSONObject categoryview2 = new JSONObject();
                Long category2Id = category2collect.getKey();
                List<BaseCategoryView> category3List = category2collect.getValue();
                categoryview2.put("categoryId",category2Id);
//                categoryview2.put("",category3List);
                categoryview2.put("categoryName",category3List.get(0).getCategory2Name());
                category1Child.add(categoryview2);

                //得到所有的三级分类的数据
                ArrayList<JSONObject>  category2Child = new ArrayList<>();
                category3List.forEach((baseCategory3View)->{
                    JSONObject categoryview3 = new JSONObject();
                    categoryview3.put("categoryId",baseCategory3View.getCategory3Id());
                    categoryview3.put("categoryName",baseCategory3View.getCategory3Name());
                    category2Child.add(categoryview3);
                });
                //将三级分类添加到二级分类中
                categoryview2.put("categoryChild",category2Child);
            }
            //将二级分类数据添加到一级分类
            categoryView1.put("categoryChild",category1Child);
            //将一级分类数据添加到存放所有数据中
            list.add(categoryView1);
        }

        return list;
    }
}
