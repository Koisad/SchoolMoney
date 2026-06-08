package com.schoolmoney.service;

import com.schoolmoney.dto.FundraiserRequest;
import com.schoolmoney.model.Fundraiser;
import com.schoolmoney.model.SchoolClass;
import com.schoolmoney.model.enums.FundraiserStatus;
import com.schoolmoney.repository.FundraiserRepository;
import com.schoolmoney.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundraiserService {

    private final FundraiserRepository fundraiserRepository;
    private final SchoolClassRepository schoolClassRepository;

    public Fundraiser createFundraiser(FundraiserRequest request, String userId) {
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        String virtualAccount = generateVirtualAccountNumber();
        while (fundraiserRepository.findByVirtualAccountNumber(virtualAccount).isPresent()) {
            virtualAccount = generateVirtualAccountNumber();
        }

        Fundraiser fundraiser = Fundraiser.builder()
                .classId(schoolClass.getId())
                .creatorId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amountPerChild(request.getAmountPerChild())
                .virtualAccountNumber(virtualAccount)
                .isPublic(request.isPublic())
                .build();

        return fundraiserRepository.save(fundraiser);
    }

    public List<Fundraiser> getClassFundraisers(String classId) {
        return fundraiserRepository.findByClassId(classId);
    }

    public Fundraiser addReceipt(String fundraiserId, String receiptUrl, String userId) {
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId)
                .orElseThrow(() -> new RuntimeException("Fundraiser not found"));

        if (!fundraiser.getCreatorId().equals(userId)) {
            throw new RuntimeException("Only creator can add receipts");
        }

        fundraiser.getReceiptUrls().add(receiptUrl);
        return fundraiserRepository.save(fundraiser);
    }

    public Fundraiser closeFundraiser(String fundraiserId, String userId) {
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId)
                .orElseThrow(() -> new RuntimeException("Fundraiser not found"));

        if (!fundraiser.getCreatorId().equals(userId)) {
            throw new RuntimeException("Only creator can close");
        }

        fundraiser.setStatus(FundraiserStatus.CLOSED);
        return fundraiserRepository.save(fundraiser);
    }

    private String generateVirtualAccountNumber() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 16);
    }
}