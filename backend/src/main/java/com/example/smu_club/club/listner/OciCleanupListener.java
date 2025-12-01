package com.example.smu_club.club.listner;

import com.example.smu_club.util.OCICleanupEvent;
import com.example.smu_club.util.OciStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OciCleanupListener {

    private final OciStorageService ociStorageService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOciCleanUp(OCICleanupEvent event) {

        ociStorageService.deleteUrls(event.getUrlsToDelete());
    }
}
