package hello.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/spring")
public class SpringUploadController {
    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile(){
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(@RequestParam String itemName,
                           // 스프링은 MultipartFile이라는 인터페이스로 멀티파트 파일을 매우 편리하게 업로드 하는 기능을 지원한다.
                           @RequestParam MultipartFile file,
    // <input type="file" name="file"> -> name의 값이 file이어서 @RequestParam("file") MultipartFile file 이렇게 써 줄 필요 없다. -> 안써줘도 된다.
                           HttpServletRequest request) throws IOException {
    // HttpServletRequest는 없어도 되는데 로그 찍으려고 가져왔다.
        log.info("request={}", request);
        log.info("itemName={}", itemName);
        log.info("multipartFile={}", file);

        if(!file.isEmpty()){
            String fullPath = fileDir+file.getOriginalFilename();
            // getOriginalFilename : spring이 제공하는 메서드
            log.info("파일 저장 fullPath={}", fullPath);
            file.transferTo(new File(fullPath));
            // 이렇게 하면 끝이다. 파일을 fullPath 경로에 저장을 해준다.
            // transferTo : 1) 파일도 지원하고 패스도 지원한다. 두가지를 지원한다.
            //              2) checked 예외 발생한다.
        }

//       참고로 만일 아래와 같은 클래스가 있고 아래의 클래스의 인스턴스를 @ModelAttribute가 주입하는 것도 가능하다.
//        public class Test{
//            String itemName,
//            MultipartFile file
//              ....
//        }
//        Argument Resolver가 다 해준다.

//        * MultipartFile 주요 메서드 *
//        file.getOriginalFilename() : 업로드 파일 명
//        file.transferTo(...) : 파일 저장
        return "upload-form";
    }
}
