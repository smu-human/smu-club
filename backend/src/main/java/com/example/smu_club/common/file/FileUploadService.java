package com.example.smu_club.common.file;


import com.example.smu_club.club.dto.UploadUrlListRequest;
import com.example.smu_club.common.fileMetaData.FileMetaDataService;
import com.example.smu_club.util.PreSignedUrlResponse;
import com.example.smu_club.util.oci.OciStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final OciStorageService ociStorageService;
    private final FileMetaDataService fileMetaDataService;

    @Transactional
    public List<PreSignedUrlResponse> prepareUploads(List<UploadUrlListRequest.FileDetail> fileDetails) {
        return fileDetails.stream()
                .map(this::createPendingFileAndUrl)
                .toList();
    }

    private PreSignedUrlResponse createPendingFileAndUrl(UploadUrlListRequest.FileDetail fileDetail) {

        String uniqueFileName = UUID.randomUUID() + "_" + fileDetail.getFileName();

        fileMetaDataService.savePending(uniqueFileName);

        return ociStorageService.createUploadPreSignedUrl(uniqueFileName, fileDetail.getContentType());
    }
}
