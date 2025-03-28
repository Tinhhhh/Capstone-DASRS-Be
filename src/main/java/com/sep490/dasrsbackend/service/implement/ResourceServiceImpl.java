package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.Resource;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewResource;
import com.sep490.dasrsbackend.model.payload.response.ListResourceResponse;
import com.sep490.dasrsbackend.model.payload.response.ListResourceResponseForAdmin;
import com.sep490.dasrsbackend.model.payload.response.ResourceResponse;
import com.sep490.dasrsbackend.model.payload.response.ResourceResponseForAdmin;
import com.sep490.dasrsbackend.repository.ResourceRepository;
import com.sep490.dasrsbackend.repository.RoundRepository;
import com.sep490.dasrsbackend.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final ModelMapper modelMapper;
    private final RoundRepository roundRepository;

    @Override
    public void newResource(NewResource request) {
        Resource resource = modelMapper.map(request, Resource.class);
        resource.setEnable(true);
        resourceRepository.save(resource);
    }

    @Override
    public void updateResource(Long id, NewResource request) {

        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Map not found"));
        modelMapper.map(request, resource);
        resourceRepository.save(resource);
    }

    @Override
    public ResourceResponse getResource(Long id) {

        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Resource not found"));
        return modelMapper.map(resource, ResourceResponse.class);
    }

    private static Pageable getPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return pageable;
    }

    @Override
    public void changeResourceStatus(Long id, boolean enable) {
        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Resource not found"));

        if (resource.isEnable() == enable) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Resource already " + enable);
        }

        Optional<Round> round = roundRepository.findByStatusAndStartDateBefore(RoundStatus.ACTIVE, DateUtil.getCurrentTimestamp());

        if (round.isPresent()) {
            if (round.get().getResource().getId().equals(id)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Resource's status can't be change while round is active");
            }
        }

        resource.setEnable(enable);
        resourceRepository.save(resource);
    }

    @Override
    public ListResourceResponseForAdmin getAllResourceForAdmin(int pageNo, int pageSize, String sortBy, String sortDirection) {

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDirection);
        Page<Resource> resources = resourceRepository.findAll(pageable);
        List<ResourceResponseForAdmin> resourceResponse = resources.stream()
                .map(resource -> {
                    ResourceResponseForAdmin rs = modelMapper.map(resource, ResourceResponseForAdmin.class);
                    rs.setLastModifiedDate(DateUtil.formatTimestamp(resource.getLastModifiedDate()));
                    rs.setCreatedDate(DateUtil.formatTimestamp(resource.getLastModifiedDate()));

                    return rs;
                })
                .toList();

        ListResourceResponseForAdmin listMapResponse = new ListResourceResponseForAdmin();
        listMapResponse.setContent(resourceResponse);
        listMapResponse.setTotalPages(resources.getTotalPages());
        listMapResponse.setTotalElements(resources.getTotalElements());
        listMapResponse.setPageSize(resources.getSize());
        listMapResponse.setPageNo(resources.getNumber());
        listMapResponse.setLast(resources.isLast());

        return listMapResponse;
    }

    @Override
    public ListResourceResponse getAllResourceForAll(int pageNo, int pageSize, String sortBy, String sortDirection) {

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDirection);

        Page<Resource> resources = resourceRepository.findByIsEnable(true, pageable);

        List<ResourceResponse> resourceResponse = resources.stream()
                .map(resource -> modelMapper.map(resource, ResourceResponse.class))
                .toList();

        ListResourceResponse listMapResponse = new ListResourceResponse();
        listMapResponse.setContent(resourceResponse);
        listMapResponse.setTotalPages(resources.getTotalPages());
        listMapResponse.setTotalElements(resources.getTotalElements());
        listMapResponse.setPageSize(resources.getSize());
        listMapResponse.setPageNo(resources.getNumber());
        listMapResponse.setLast(resources.isLast());

        return listMapResponse;
    }

}
