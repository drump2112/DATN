package com.example.DATN.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {
	private static final Logger log = LoggerFactory.getLogger(ImageService.class);
	private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

	public String saveImage(MultipartFile file, String subFolder) throws IOException {
		if (file == null || file.isEmpty()) {
			log.info("Tệp rỗng hoặc null");
			return null;
		}
		try {
			String folderPath = BASE_UPLOAD_DIR + subFolder;
			File folder = new File(folderPath);
			if (!folder.exists()) {
				folder.mkdirs();
				log.info("Tạo thư mục: {}", folderPath);
			}

			String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

			File dest = new File(folderPath, uniqueFileName);

			log.info("Chuẩn bị lưu tệp vào: {}", dest.getAbsolutePath());

			file.transferTo(dest);
			if (!dest.exists()) {
				throw new IOException("Tệp không được lưu tại: " + dest.getAbsolutePath());
			}

			log.info("Tệp đã lưu thành công: {}", dest.getAbsolutePath());
			return "/uploads/" + subFolder + "/" + uniqueFileName;
		} catch (IOException e) {
			log.error("Lỗi lưu ảnh: {}", e.getMessage(), e);
			throw new IOException("Lỗi lưu ảnh: " + e.getMessage(), e);
		}
	}

	public void deleteImage(String imagePath) throws IOException {
		if (imagePath != null && !imagePath.isEmpty()) {
			if (imagePath.startsWith("/")) {
				imagePath = imagePath.substring(1);
			}

			Path path = Paths.get(imagePath);
			Files.deleteIfExists(path);
		}
	}
}
