package hello.upload.controller;

import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.domain.UploadFile;
import hello.upload.file.FileStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    // 우리가 만들었던 ItemRepository와 FileStore을 의존관계 주입으로 넣는다.
    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form){
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {

        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

//     여기서 중요한게 있는데 파일은 DB에 저장하지 않고 보통 그냥 스토리지에 저장을 한다. AWS를 쓰면 S3에 저장을 한다.
//     DB에는 파일에 저장된 경로만, 그정도만 저장하는 것이다. 실제 파일 자체를 저장하지는 않는다. 경로도 보통
//     경로의 full path를 저장하지 않고 경로의 full path는 어딘가에 저장해 놓고 그 이후의 상대적인 경로만 저장해 놓는다.

        // 데이터베이스에 저장
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
    }

    // ======================================================================================================
    // 업로드 저장은 됐고 이제 다운로드 기능과 업로드(저장)이 되었다는 것을 보여주는 기능을 만들어 보겠다.
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model){
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }
// 지금 바로 위의 코드로 인해서 item-view.html이 잘 보이는 것을 확인할 수 있다. 하지만 첨부파일명을 클릭했을때 처리하는 컨트롤러가 없고
//      <img
//            th:each="imageFile : ${item.imageFiles}"
//            th:src="|/images/${imageFile.getStoreFileName()}|"
//            width="300"
//            height="300"
//      />
//    localhost:8080/images/${imageFile.getStoreFileName() 을 처리하는 컨트롤러도 없기 때문에 브라우저에서 다운로드 하는 기능도 구현이 안되고
//    브라우저가 이미지를 랜더링 하는 기능도 보여지지 않는다. 우선 나는 브라우저가 /images/123dfdvsdwefec.png 이렇게 요청하면 서버가 이미지 파일을 주도록 컨트롤러를
//    만들어 보겠다. 바로 아래가 그것을 구현한 컨트롤러 이다.
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:"+fileStore.getFullPath(filename));
    }
//    와 ㅎㅎㅎ 브라우저가 이미지를 잘 보여준당~~ ㅎㅎ <img>의 th:src="|/images/${imageFile.getStoreFileName()}|" 이런 속성으로
//    인해서 브라우저(DOM)가 화면을 랜더링 하는 과정에서 /images/1234sdvdasfd.png 이렇게 서버에 요청을 하게 된다. 나는 로컬서버니까
//    localhost:8080/images/1234sdvdasfd.png 이렇게 요청이 가겠다. 그럼 바로 위에 있는 컨트롤러가 이를 받는다. 우선 @ResponseBody가 있으니까
//    ViewResolver을 거치지 않고 응답메시지 바디에 직접 데이터가 들어가겠다. 어떤 데이터를 넣어야 할까? 당연히 1234sdvdasfd.png의 바이너리 코드를 넣어야 겠다. ㅎㅎ
//    그래서 UrlResource라는 클래스를 이용했다. UrlResource의 생성자 인자에 file: 과 서버컴퓨터의 파일이 저장되어 있는 구체적인 전체 경로를 붙여서 인자로 넣어주면
//    해당경로에 있는 파일을 찾아서 바이너리코드로 바꾼다음에 응답메시지 바디에 넣어준다. ㅎㅎ 참고로 UrlResource객체를 받을 수 있는 Resource인터페이스는 spring에서 제공하는
//    인터페이스를 써야 하고 바로 위의 메소드는 이미지를 응답메시지바디에 잘 넣어서 보내주지만 코드가 보안에 취약해 보이니까 여러가지 체크 로직을 넣는것이 더 좋다.

//    이번에는 이미지와 같이 브라우저가 이미지를 그려주는 것이 아니라 파일이(여기서는 이미지가 아니라 다른 파일 ,, pdf, txt 같은것)
//    실제로 다운로드 되는 기능을 구현해 보자
//    첨부파일: <a
//                th:if="${item.attachFile}"
//                th:href="|/attach/${item.id}|"
//                th:text="${item.getAttachFile().getUploadFileName()}"
//            /><br />
//    이렇게 코드가 생겼기 때문에 사용자가 th:text="${item.getAttachFile().getUploadFileName()}" 를 누르면
//    th:href="|/attach/${item.id}|" 로 요청이 가겠다.
//   * 그럼 해당 요청을 받는 메소드가 이를 처리(실제 다운로드 되는 기능 구현)해 보자. *

    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException{
//    @ResponseBody를 써도 되는데 여기서는 다른것을 보여주기 위해서 ResponseEntity<Resource>를 썼다. 그리고 헤더에 뭔가 추가할게 있어서 썼다.
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();
        // 실제 사용자가 다운로드 받을때에는 업로드한 파일명이 나와야 하기 때문에 uploadFileName이라는 변수에 업로드한 파일명을 담았다.

        UrlResource resource = new UrlResource("file:"+fileStore.getFullPath(storeFileName));
        log.info("uploadFileName={}", uploadFileName);

//        UriUtils : 스프링이 제공하고 수 많은 인코딩 기능들을 제공한다.
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
//        한글이나 특수문자들이 깨밀수 있기 때문에, 브라우저마다 다르기는 한데, 인코딩을 해주고 인코딩 된 파일명을 넣어줘야 한다.
        String contentDisposition = "attachment; filename=\""+encodedUploadFileName+"\"";

//        ResponseEntity.ok() 이것을 반환하면 응답메시지가 반환이 되는 것이다. 이때 우리가 함수 내에서 만들어 줬던 헤더와 바디를 담아서 보내면 된다.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
//        만일 헤더( .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition) ) 를 안넣으면 다운로드는 안되고 파일의 내용을 브라우저에서
//        열어서 보이기만 하는 것을 확인할 수 있다. 우리는 다운로드 받는 것을 원하기 때문에 헤더를 넣어줘야 한다. * 이것은 그냥 규약이다 *.
//        헤더를 넣어주면 다운로드가 정상적으로 실행되는 것을 확인할 수 있다.
//         => 브라우저가 content-disposition을 보고 다운로드 할지를 결정하는 것이다.

        // 2023 10 04 질문(궁금)사항 :  어떻게 ResponseEntity.ok() 다음에 점(.)으로 header과 body를 호출 할 수 있는지가 궁금하다.
        //                            ResponseEntity.ok() 메서드를 들어가 보면 안될 것 같다. ㅎㅎ
    }
}

















