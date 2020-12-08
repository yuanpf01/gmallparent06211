package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yuanpf
 * @create 2020-11-30 13:39
 */
@Api(tags = "后台数据接口")
@RequestMapping("/admin/product/")
@RestController
//@CrossOrigin
public class BaseManageController {

    @Autowired
    private ManageService manageService;

    //1、查询所有的一级分类http://api.gmall.com/admin/product/getCategory1
    @GetMapping("getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> category1 = manageService.getCategory1();
        return Result.ok(category1);
    }
    //2、查询所有的二级分类http://api.gmall.com/admin/product/getCategory2/{category1Id}
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> category2 = manageService.getCategory2(category1Id);
        return Result.ok(category2);
    }
    //3、查询所有的三级分类http://api.gmall.com/admin/product/getCategory3/{category2Id}
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> category3 = manageService.getCategory3(category2Id);
        return Result.ok(category3);
    }
    //4、根据分类id查询平台属性及平台属性值http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                                @PathVariable Long category2Id,
                               @PathVariable Long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }

    //5、添加平台属性
    /**
     *  http://api.gmall.com/admin/product/saveAttrInfo
     *     Request Method: POST
     */

    //http://api.gmall.com/admin/product/saveAttrInfo
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    //6、修改平台属性
    //http://api.gmall.com/admin/product/getAttrValueList/{attrId}
    //select * from base_attr_info bai where bai.id = ?
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        //可以通过attrid 获取平台属性，如果属性存在，则获取属性中对应的属性值getattrvalueList
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        //查询平台属性值的集合
//        List<BaseAttrValue> baseAttrInfoList = manageService.getAttrValueList(attrId);
        return Result.ok(baseAttrInfo.getAttrValueList());
    }



    //http://api.gmall.com/admin/product/ {page}/{limit}?category3Id=61
    /**
     * 获取category3Id=61的几种方式？
     *获取spu分页列表
     */

    @GetMapping("/product/{page}/{limit}")
    public Result  getSpuInfoPageList(@PathVariable Long page,
                                      @PathVariable Long limit,
                                      SpuInfo spuInfo){
        Page<SpuInfo> page1 = new Page(page,limit);
        IPage spuInfoPageList = manageService.getSpuInfoPageList(page1,spuInfo);
        return Result.ok(spuInfoPageList);
    }
}
