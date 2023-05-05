package dv.kinash.hw15.config;

import dv.kinash.hw15.dto.BookInfoDTO;
import dv.kinash.hw15.dto.UserDTO;
import dv.kinash.hw15.repository.entity.Book;
import dv.kinash.hw15.repository.entity.User;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper getModelMapper(){
        ModelMapper mapper = new ModelMapper();
        mapper.addConverter(new Converter<Book, BookInfoDTO>() {
            @Override
            public BookInfoDTO convert(MappingContext<Book, BookInfoDTO> mappingContext) {
                Book book = mappingContext.getSource();
                BookInfoDTO infoDTO = mappingContext.getDestination();
                if (infoDTO == null)
                    infoDTO = new BookInfoDTO();
                infoDTO.setId(book.getId());
                infoDTO.setInfo(String.format(
                        "%s by %s (ISBN: %d)",
                        book.getDescription(),
                        book.getAuthor(),
                        book.getISBN()));
                return infoDTO;
            }
        });
        mapper.addConverter(new Converter<User, UserDTO>() {
            @Override
            public UserDTO convert(MappingContext<User, UserDTO> mappingContext) {
                User user = mappingContext.getSource();
                UserDTO userDTO = mappingContext.getDestination();
                if (userDTO == null)
                    userDTO = new UserDTO();
                userDTO.setRole(user.getIsAdmin() ? "admin" : "reader");
                userDTO.setName(user.getName());
                userDTO.setEmail(user.getEmail());
                return userDTO;
            }
        });
        return mapper;
    }
}
