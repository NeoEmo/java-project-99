package hexlet.code.app.service;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelPatchDTO;
import hexlet.code.app.exception.ResourceAlreadyExistException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Object, т.к. нету LabelParamsDTO

@Service
@Transactional
public class LabelService implements CRUDService<LabelDTO, LabelCreateDTO, LabelPatchDTO, Object> {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    @Autowired
    public LabelService(LabelRepository labelRepository, LabelMapper labelMapper) {
        this.labelRepository = labelRepository;
        this.labelMapper = labelMapper;
    }

    @Override
    public List<LabelDTO> index() {
        return labelRepository.findAll().stream().map(labelMapper::toDTO).toList();
    }


    @Override
    public LabelDTO findById(Long id) {
        return labelRepository.findById(id).map(labelMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
    }

    @Override
    public LabelDTO create(LabelCreateDTO data) {
        if (labelRepository.findByName(data.getName()).isPresent()) {
            throw new ResourceAlreadyExistException("Label with name " + data.getName() + " already exists");
        }
        var label = labelMapper.toEntity(data);
        return labelMapper.toDTO(labelRepository.save(label));
    }

    @Override
    public LabelDTO update(LabelPatchDTO data, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));

        if (data.getName() != null && data.getName().isPresent() && data.getName().get() != null) {
            var newName = data.getName().get();
            var existingName = labelRepository.findByName(newName);
            if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
                throw new ResourceAlreadyExistException("Label with name " + newName + " already exists");
            }
        }

        labelMapper.update(data, label);
        return labelMapper.toDTO(labelRepository.save(label));
    }

    @Override
    public void delete(Long id) {
        if (!labelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Label with id " + id + " not found");
        }
        labelRepository.deleteById(id);
    }
}
