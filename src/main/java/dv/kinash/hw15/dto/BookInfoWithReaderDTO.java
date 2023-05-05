package dv.kinash.hw15.dto;

import lombok.Data;

@Data
public class BookInfoWithReaderDTO extends BookInfoDTO {
    private Boolean hasReader;
}
