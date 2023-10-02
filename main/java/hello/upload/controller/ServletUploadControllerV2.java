package hello.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.Part;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {
    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile(){
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws ServletException, IOException{
        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);

        for (Part part : parts) {
            log.info("==== PART ====");
            log.info("name={}", part.getName());
            // part도 Header와 Body로 구분된다.
           Collection<String> headerNames = part.getHeaderNames();
            for(String headerName : headerNames){
                log.info("header {}: {}", headerName, part.getHeader(headerName));
            }
            // 편의 메서드
            // content-disposition; filename
            log.info("submittedFileName={}", part.getSubmittedFileName());
            log.info("size={}", part.getSize()); // part에 있는 body의 크기(size)

            //part의 body에 있는 데이터 읽기
            InputStream inputStream = part.getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            // -> 바이너리를 문자로 바꾸던 문자를 바이너리로 바꾸던 항상 charset을 지정해 줘야 한다.
            log.info("body={}", body);

            // 파일에 저장하기
            if(StringUtils.hasText(part.getSubmittedFileName())){
                // 여러가지 방법이 있겠지만 StringUtils.hasText()를 사용했다.
                // part의 Content-Disposition헤더의 값들중 하나인 filename의 값이 있는가?
                String fullPath = fileDir+part.getSubmittedFileName();
                log.info("파일 저장 fullPath={}", fullPath);
                part.write(fullPath);
                // -> part는 write라는 것을 제공하는데 그곳에 경로를 넣어주면 된다.
            }
        }
        return "upload-form";
    }


}
