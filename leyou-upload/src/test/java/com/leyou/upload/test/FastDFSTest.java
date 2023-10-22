package com.leyou.upload.test;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastDFSTest {
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private ThumbImageConfig thumbImageConfig;
    @Test
    public void testUpload() throws FileNotFoundException{
        File file = new File("D:\\upload.jpg");
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(new FileInputStream(file), file.length(), "png", null);
        String fullPath = storePath.getFullPath();
        String path = storePath.getPath();
        System.out.println("原图的路径,带组别" + fullPath); //原图的路径,带组别
        System.out.println("不带组别的原图的路径" + path);//不带组别的原图的路径

        //获取缩略图的路径
        String thumbImagePath = thumbImageConfig.getThumbImagePath(storePath.getPath());//不带组别，获取缩略图的路径
        System.out.println("缩略图路径" + thumbImagePath);


    }
}
