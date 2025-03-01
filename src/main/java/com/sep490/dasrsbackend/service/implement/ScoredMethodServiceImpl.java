package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.ScoredMethod;
import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewScoreMethod;
import com.sep490.dasrsbackend.model.payload.response.ListScoredMethod;
import com.sep490.dasrsbackend.model.payload.response.ScoredMethodResponse;
import com.sep490.dasrsbackend.repository.ScoredMethodRepository;
import com.sep490.dasrsbackend.service.ScoredMethodService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoredMethodServiceImpl implements ScoredMethodService {
    private final ScoredMethodRepository scoredMethodRepository;
    private final ModelMapper modelMapper;

    @Override
    public void createNewScoredMethod(NewScoreMethod newScoreMethod) {
        ScoredMethod scoredMethod = modelMapper.map(newScoreMethod, ScoredMethod.class);
        scoredMethod.setStatus(ScoredMethodStatus.ACTIVE);
        scoredMethodRepository.save(scoredMethod);
    }

    @Override
    public ScoredMethodResponse getScoredMethod(Long scoredMethodId) {

        if (scoredMethodId == null) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method Id is required");
        }

        ScoredMethod scoredMethod = scoredMethodRepository.findById(scoredMethodId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method not found"));
        return modelMapper.map(scoredMethod, ScoredMethodResponse.class);

    }

    @Override
    public void updateScoredMethod(Long scoredMethodId, NewScoreMethod newScoreMethod) {

        if (scoredMethodId == null) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method Id is required");
        }

        ScoredMethod scoredMethod = scoredMethodRepository.findById(scoredMethodId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method not found"));
        scoredMethod = modelMapper.map(newScoreMethod, ScoredMethod.class);
        scoredMethodRepository.save(scoredMethod);
    }

    @Override
    public ListScoredMethod getAllScoredMethods(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<ScoredMethod> scoredMethods = scoredMethodRepository.findAll(pageable);
        List<ScoredMethodResponse> scoredMethodResponses = scoredMethods.stream()
                .map(scoredMethod -> modelMapper.map(scoredMethod, ScoredMethodResponse.class))
                .toList();

        ListScoredMethod listScoredMethod = new ListScoredMethod();
        listScoredMethod.setContent(scoredMethodResponses);
        listScoredMethod.setTotalPages(scoredMethods.getTotalPages());
        listScoredMethod.setTotalElements(scoredMethods.getTotalElements());
        listScoredMethod.setPageNo(scoredMethods.getNumber());
        listScoredMethod.setPageSize(scoredMethods.getSize());
        listScoredMethod.setLast(scoredMethods.isLast());

        return listScoredMethod;
    }
}
