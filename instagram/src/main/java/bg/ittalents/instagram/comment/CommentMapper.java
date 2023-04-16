package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentWithRepliesDTO;
import bg.ittalents.instagram.comment.DTOs.CommentWithoutRepliesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    // DTO to entity mappings
    Comment commentWithoutRepliesDtoToComment(CommentWithoutRepliesDTO commentWithoutRepliesDto);
    Comment commentWithRepliesDtoToComment(CommentWithRepliesDTO commentWithRepliesDto);

    // Entity to DTO mappings
    CommentWithoutRepliesDTO commentToCommentWithoutRepliesDto(Comment comment);
    CommentWithRepliesDTO commentToCommentWithRepliesDto(Comment comment);
}
