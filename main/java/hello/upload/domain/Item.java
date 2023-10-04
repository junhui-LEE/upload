package hello.upload.domain;

import lombok.Data;

import java.util.List;

@Data
public class Item {
    // DB 테이블의 도메인이다.
    private Long id;
    private String itemName;
    private UploadFile attachFile;
    private List<UploadFile> imageFiles;
}
