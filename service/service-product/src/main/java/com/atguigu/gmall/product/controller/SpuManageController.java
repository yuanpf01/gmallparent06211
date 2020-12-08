package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseSaleAttrService;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;

/**
 * @author yuanpf
 * @create 2020-12-01 12:51
 */
@Api(tags = "spuInfo数据接口")
@RestController
@RequestMapping("/admin/product/")
public class SpuManageController {

    @Value("${fileServer.url}")
    private  String fileServerUrl;//文件上传时的文件服务ip

    @Autowired
    ManageService manageService;
    @Autowired
    SpuInfoService spuInfoService;
    @Autowired
    BaseSaleAttrService baseSaleAttrService;

    //http://api.gmall.com/admin/product/ {page}/{limit}?category3Id=61
    /**
     * 获取category3Id=61的几种方式？
     *获取spu分页列表
     *
     */

    @GetMapping("{page}/{limit}")
    public Result  getSpuInfoPageList(@PathVariable Long page,
                                      @PathVariable Long limit,
                                      SpuInfo spuInfo){
        Page<SpuInfo> page1 = new Page(page,limit);
        IPage spuInfoPageList = manageService.getSpuInfoPageList(page1,spuInfo);
        return Result.ok(spuInfoPageList);
    }
    /**
     * 获取销售属性
     * http://api.gmall.com/admin/product/baseSaleAttrList
     *
     */
    @GetMapping("baseSaleAttrList")
    public Result getbaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrService.list(null);
        return Result.ok(baseSaleAttrList);
    }
    /**
     * 根据spuid获取销售属性
     * http://api.gmall.com/admin/product/spuSaleAttrList/17
     */
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = spuInfoService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }
    /**
     * 根据spuid获取平台属性
     * http://api.gmall.com/admin/product/spuSaleAttrList/17
     */
//    @GetMapping("spuSaleAttrList/{spuId}")
//    public Result getSpuSaleAttrList(@PathVariable Long spuId){
//        List<SpuSaleAttr> spuSaleAttrList = spuInfoService.getSpuSaleAttrList(spuId);
//        return Result.ok(spuSaleAttrList);
//    }

    /**
     * 根据spuid获取图片列表
     * http://api.gmall.com/admin/product/spuImageList/{spuId}
     *
     */
    @GetMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId){
        List<SpuImage> spuImageList = spuInfoService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }
    /**
     * 添加spu
     * http://api.gmall.com/admin/product/saveSpuInfo
     * post
     */
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuInfoService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * 文件上传
     * http://api.gmall.com/admin/product/fileUpload
     *请求参数file
     * 请求方式 post
     *
     */

    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws IOException, MyException {

        //读取tracker的内容进行初始化
        //String file1 = this.getClass().getResource("/tracker.conf").getFile();
        //System.out.println("file1 = " + file1);
        String configfile = this.getClass().getResource("/tracker.conf").getFile();
        String path = null;
        if (configfile != null) {
//            初始化
            ClientGlobal.init(configfile);
            //创建一个trackerClient
            TrackerClient trackerClient = new TrackerClient();
            //创建一个trackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建一个storageClient1
            StorageClient1 storageClient1 = new StorageClient1();
            //上传
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            storageClient1.upload_appender_file1(file.getBytes(),extension,null);
            //  获取上上传之后的url group1/M00/00/01/wKjIgF9zVUOEAQ_2AAAAAPzxDAI115.png
            path = storageClient1.upload_appender_file1(file.getBytes(), originalFilename, null);
            //返回
            System.out.println("====================" + fileServerUrl + path);
        }
        return Result.ok(fileServerUrl + path);
    }

}
