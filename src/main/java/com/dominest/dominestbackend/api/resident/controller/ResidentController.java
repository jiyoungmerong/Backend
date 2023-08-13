package com.dominest.dominestbackend.api.resident.controller;


import com.dominest.dominestbackend.api.common.ResTemplate;
import com.dominest.dominestbackend.api.resident.dto.PdfBulkUploadDto;
import com.dominest.dominestbackend.api.resident.dto.ResidentListDto;
import com.dominest.dominestbackend.api.resident.dto.ResidentPdfListDto;
import com.dominest.dominestbackend.api.resident.dto.SaveResidentDto;
import com.dominest.dominestbackend.api.resident.util.PdfType;
import com.dominest.dominestbackend.domain.resident.Resident;
import com.dominest.dominestbackend.domain.resident.ResidentService;
import com.dominest.dominestbackend.domain.resident.component.ResidenceSemester;
import com.dominest.dominestbackend.global.exception.ErrorCode;
import com.dominest.dominestbackend.global.exception.exceptions.BusinessException;
import com.dominest.dominestbackend.global.exception.exceptions.file.FileIOException;
import com.dominest.dominestbackend.global.util.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ResidentController {

    private final ResidentService residentService;
    private final FileService fileService;

    // 엑셀로 업로드
    @PostMapping("/residents/upload-excel")
    public ResponseEntity<ResTemplate<?>> handleFileUpload(@RequestParam(required = true) MultipartFile file
                                                                                                            , @RequestParam(required = true) ResidenceSemester residenceSemester){
        residentService.excelUpload(file, residenceSemester);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 전체조회
    @GetMapping("/residents")
    public ResTemplate<ResidentListDto.Res> handleGetAllResident(@RequestParam(required = true) ResidenceSemester residenceSemester){
        ResidentListDto.Res residents = residentService.getAllResidentByResidenceSemester(residenceSemester);
        return new ResTemplate<>(HttpStatus.OK, "입사생 목록 조회 성공", residents);
    }

    // (테스트용) 입사생 데이터 전체삭제
    @DeleteMapping("/residents")
    public ResponseEntity<ResTemplate<?>> handleDeleteAllResident(){
        residentService.deleteAllResident();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 입사생 단건 등록. 단순 DTO 변환 후 저장만 하면 될듯
    @PostMapping("/residents")
    public ResponseEntity<ResTemplate<?>> handleSaveResident(@RequestBody @Valid SaveResidentDto.Req reqDto){
        residentService.saveResident(reqDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 입사생 수정
    @PatchMapping("/residents/{id}")
    public ResponseEntity<ResTemplate<?>> handleUpdateResident(@PathVariable Long id, @RequestBody @Valid SaveResidentDto.Req reqDto){
        try {
            residentService.updateResident(id, reqDto.toEntity());
        }catch (DataIntegrityViolationException e) {
            throw new BusinessException("입사생 정보 변경 실패, 잘못된 입력값입니다. 데이터 누락 혹은 중복을 확인해주세요.", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 입사생 삭제
    @DeleteMapping("/residents/{id}")
    public ResponseEntity<ResTemplate<?>> handleDeleteResident(@PathVariable Long id){
        residentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 특정 입사생의 PDF 조회
    @GetMapping("/residents/{id}/pdf")
    public ResTemplate<?> handleGetPdf(@PathVariable Long id,  @RequestParam(required = true) PdfType pdfType,
                                                                        HttpServletResponse response){
        // filename 가져오기.
        Resident resident = residentService.findById(id);

        // PdfType에 따라 입사 혹은 퇴사신청서 filename 가져오기
        String filename = pdfType.getPdfFileName(resident);
        FileService.FilePrefix filePrefix = pdfType.toFilePrefix();

        // PDF 파일 읽기
        byte[] bytes = fileService.getByteArr(filePrefix, filename);

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);

        try(ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new FileIOException(ErrorCode.FILE_CANNOT_BE_SENT);
        }
        return new ResTemplate<>(HttpStatus.OK, "pdf 조회 성공");
    }

    // PDF 단건 업로드
    @PostMapping("/residents/{id}/pdf")
    public ResponseEntity<ResTemplate<String>> handlePdfUpload(@PathVariable Long id, @RequestParam(required = true) MultipartFile pdf,
                                                                                                                    @RequestParam(required = true) PdfType pdfType){
        FileService.FilePrefix filePrefix = pdfType.toFilePrefix();

        residentService.uploadPdf(id, filePrefix, pdf);
        ResTemplate<String> resTemplate = new ResTemplate<>(HttpStatus.CREATED, "pdf 업로드 완료");
        return ResponseEntity.created(URI.create("/residents/"+id+"/pdf")).body(resTemplate);
    }

    // PDF 전체 업로드
    @PostMapping("/residents/pdf")
    public ResponseEntity<ResTemplate<PdfBulkUploadDto.Res>> handlePdfUpload(@RequestParam(required = true) List<MultipartFile> pdfs
                                                                                                                    , @RequestParam(required = true) ResidenceSemester residenceSemester
                                                                                                                    , @RequestParam(required = true) PdfType pdfType){
        FileService.FilePrefix filePrefix = pdfType.toFilePrefix();
        PdfBulkUploadDto.Res res = residentService.uploadPdfs(filePrefix, pdfs, residenceSemester);

        ResTemplate<PdfBulkUploadDto.Res> resTemplate = new ResTemplate<>(HttpStatus.CREATED,
                "pdf 업로드 완료. 저장된 파일 수: " + res.getSuccessCount() + "개", res);
        return ResponseEntity.created(URI.create("/residents/pdf")).body(resTemplate);
    }

    // 해당차수 입사생 전체 PDF 조회
    @GetMapping("/residents/pdf")
    public ResTemplate<ResidentPdfListDto.Res> handleGetAllPdfs(@RequestParam(required = true) ResidenceSemester residenceSemester){
        ResidentPdfListDto.Res res = residentService.getAllPdfs(residenceSemester);
        return new ResTemplate<>(HttpStatus.OK, "pdf url 조회 성공", res);
    }
}

















