package likelion.madi.service;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.Treatment;
import likelion.madi.domain.User;
import likelion.madi.dto.request.CareCardCreateRequest;
import likelion.madi.dto.response.CareCardCreateResponse;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.TreatmentRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CareCardService {
    private final CareCardRepository careCardRepository;
    private final TreatmentRepository treatmentRepository;
    private final UserRepository userRepository;

    public CareCardCreateResponse create(Long userId, CareCardCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        CareCard careCard = CareCard.builder()
                .user(user)
                .treatmentDate(request.getTreatmentDate())
                .build();

        for (CareCardCreateRequest.TreatmentEntry entry : request.getTreatments()) {
            if (entry.getTreatmentId() == null && entry.getCustomName() == null) {
                throw new BadRequestException("treatment_id 또는 custom_name 중 하나는 입력해야 합니다.");
            }

            Treatment treatment = null;
            if (entry.getTreatmentId() != null) {
                treatment = treatmentRepository.findById(entry.getTreatmentId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_TREATMENT));
            }

            careCard.addTreatment(CareCardTreatment.builder()
                    .careCard(careCard)
                    .treatment(treatment)
                    .customName(entry.getCustomName())
                    .build());
        }

        careCardRepository.save(careCard);

        return CareCardCreateResponse.from(careCard);
    }
}
