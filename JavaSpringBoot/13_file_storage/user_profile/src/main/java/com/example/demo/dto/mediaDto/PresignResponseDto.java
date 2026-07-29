package com.example.demo.dto.mediaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignResponseDto {
    private String mediaId;
    private String uploadUrl;
}