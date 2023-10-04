package hello.upload.controller;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ItemForm {
    // 상품 저장용 폼이다.
    // List<MultipartFile> imageFiles : 이미지를 다중 업로드 하기 위해 MultipartFile를 사용했다.
    // MultipartFile attachFile : 멀티파트는 @ModelAttribute에서 사용할 수 있다.
    private Long itemId;
    private String itemName;
    private List<MultipartFile> imageFiles;
    private MultipartFile attachFile;
}
