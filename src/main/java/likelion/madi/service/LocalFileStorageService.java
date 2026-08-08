package likelion.madi.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.exception.InternalServerException;
import likelion.madi.common.response.ErrorStatus;


@Component
public class LocalFileStorageService {
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB
    private static final Path UPLOAD_DIR = Path.of("uploads");

    public String store(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(ErrorStatus.BAD_REQUEST_INVALID_IMAGE_SIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException(ErrorStatus.BAD_REQUEST_NOT_SUPPORTED_MEDIA_TYPE);
        }

        try {
            Files.createDirectories(UPLOAD_DIR);

            String ext = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + ext;

            Path target = UPLOAD_DIR.resolve(filename);
            file.transferTo(target);

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new InternalServerException(ErrorStatus.IMAGE_UPLOAD_FAILED);
        }
    }
}
