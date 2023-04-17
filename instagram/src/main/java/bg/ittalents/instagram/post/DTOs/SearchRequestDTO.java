package bg.ittalents.instagram.post.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchRequestDTO {

    private String searchString;
    private int page;
    private int size;
}
