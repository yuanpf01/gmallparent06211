package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author yuanpf
 * @create 2020-12-01 13:16
 * http://api.gmall.com/admin/product/baseTrademark/getTrademarkList
 *
 */
@RestController
@RequestMapping("/admin/product/baseTrademark/")
@Api(tags = "获取品牌属性BaseTradeMarkController")
public class BaseTradeMarkController {

    @Autowired
    ManageService manageService;

    @Autowired
    BaseTrademarkService baseTrademarkService;
    /**
     * 分页查询品牌列表信息
     * @param page
     * @param limit
     * @return
     * http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
     *
     */
    @GetMapping("/{page}/{limit}")
    public Result getTrademarkList(@PathVariable Long page,
                                   @PathVariable Long limit
                                   ){
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page,limit);
        IPage baseTrademarkPageList = manageService.getBaseTrademarkPage(baseTrademarkPage);
        return Result.ok(baseTrademarkPageList);
    }
    /**
     * 添加品牌
     * http://api.gmall.com/admin/product/baseTrademark/save
     * post
     * http://api.gmall.com/admin/product/baseTrademark/save
     * @PostMapping("/save/{baseTrademark}")
     */
    @PostMapping("/save")
    public Result saveBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }
    /**
     * 修改品牌
     * http://api.gmall.com/admin/product/baseTrademark/update
     * 请求参数：baseTrademark的json字符串
     * put
     */
    @PutMapping("/update")
    public Result updateBaseTrademark (@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }
    /**
     * 删除品牌
     * http://api.gmall.com/admin/product/baseTrademark/remove/{id}
     * 请求参数：品牌Id
     * delete
     */
    @DeleteMapping("remove/{id}")
    public Result deleteBaseTrademark (@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
    /**
     * 5、根据Id获取品牌
     * http://api.gmall.com/admin/product/baseTrademark/get/{id}
     * 请求参数：品牌Id
     * get
     */
    @GetMapping("get/{id}")
    public Result getBaseTrademarkById (@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }
    /**
     * 获取品牌属性
     * http://api.gmall.com/admin/product/baseTrademark/getTrademarkList
     * get
     *
     */
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){

        return Result.ok(baseTrademarkService.list(null));
    }
}
