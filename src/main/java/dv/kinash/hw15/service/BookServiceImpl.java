package dv.kinash.hw15.service;

import dv.kinash.hw15.dto.BookInfoDTO;
import dv.kinash.hw15.dto.BookInfoWithReaderDTO;
import dv.kinash.hw15.dto.BookNewDTO;
import dv.kinash.hw15.exception.BookNotFoundException;
import dv.kinash.hw15.exception.BookReturnException;
import dv.kinash.hw15.repository.BookRepository;
import dv.kinash.hw15.repository.entity.Book;
import dv.kinash.hw15.repository.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final ModelMapper mapper;
    private final BookRepository repository;
    private final UserService userService;
    @Override
    public BookInfoDTO addGetNewBook(BookNewDTO newBook) {
        Book book = repository.save(mapper.map(newBook, Book.class));
        return mapper.map(book, BookInfoDTO.class);
    }
    @Override
    public void deleteBook(Long bookId) {
        if (repository.existsById(bookId)) {
            repository.deleteById(bookId);
        } else {
            throw new BookNotFoundException(String.format("Book with id=%n not found", bookId));
        }
    }
    @Override
    public List<BookInfoDTO> getUserBook(String userEmail) {
        User reader = userService.getUserByEmail(userEmail);
        return mapper.map(repository.findByReader(reader),
                new TypeToken<List<BookInfoDTO>>() {}.getType());
    }
    @Override
    public List<BookInfoDTO> getAvailableBook(String userEmail) {
        return mapper.map(repository.findByReader(null),
                new TypeToken<List<BookInfoDTO>>() {}.getType());
    }
    @Override
    public List<BookInfoWithReaderDTO> getAllBook() {
        return mapper.map(repository.findAllWithReader(),
                new TypeToken<List<BookInfoWithReaderDTO>>() {}.getType());
    }
    @Override
    public void takeBook(String userEmail, Long bookId) {
        User reader = userService.getUserByEmail(userEmail);
        Book book = repository.findById(bookId).orElseThrow(
                () -> new BookNotFoundException(
                        String.format("Book with id=%n not found", bookId)));
        book.setReader(reader);
        repository.save(book);
    }
    @Override
    public void returnBook(String userEmail, Long bookId) {
        User reader = userService.getUserByEmail(userEmail);
        Book book = repository.findById(bookId).orElseThrow(
                () -> new BookNotFoundException(
                        String.format("Book with id=%n not found", bookId)));
        if (book.getReader() == null)
            throw new BookReturnException("Book %s by %s is already returned");
        else if (! book.getReader().equals(reader))
            throw new BookReturnException("Book %s by %s taken by another reader");
        book.setReader(null);
        repository.save(book);
    }
}
