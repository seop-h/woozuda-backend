package com.woozuda.backend.shortlink.dto.note;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class NoteIdDto {
    private List<Long> id;
}
