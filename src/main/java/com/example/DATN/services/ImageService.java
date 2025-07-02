package com.example.DATN.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageService {

	private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

	public String saveImage(MultipartFile file, String subFolder) {
		if (file == null || file.isEmpty())
			return null;
		try {
			String folderPath = BASE_UPLOAD_DIR + subFolder;
			File folder = new File(folderPath);
			if (!folder.exists())
				folder.mkdirs();

			String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
			Path filePath = Paths.get(folderPath, uniqueFileName);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// Trả về path tương đối để lưu vào DB, VD: /uploads/user/xxx.jpg
			return "/uploads/" + subFolder + "/" + uniqueFileName;
		} catch (IOException e) {
			throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage(), e);
		}
	}
}
