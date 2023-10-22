package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@Service
public class UploadService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    final static List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif","image/png");
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UploadService.class);
    public String upload(MultipartFile file) {
        //校验文件大小，暂时不需要

        //校验文件类型
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if(!CONTENT_TYPES.contains(contentType)){
            LOGGER.info("{} 文件类型错误!", originalFilename);
            return null;
        }
        //校验文件内容
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null){
                LOGGER.info("文件内容不合法：{}", originalFilename);
                return null;
            }
            // 上传文件
//            file.transferTo(new File("d:\\leyou\\images\\" + originalFilename));
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = this.fastFileStorageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(), ext, null);

            // 生成url地址，返回
//            return "http://image.leyou.com/" + originalFilename;

            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部错误：{}", originalFilename);
            e.printStackTrace();
        }

        return null;

    }
}
