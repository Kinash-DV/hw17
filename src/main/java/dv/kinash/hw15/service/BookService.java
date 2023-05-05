package dv.kinash.hw15.service;

import dv.kinash.hw15.dto.BookInfoDTO;
import dv.kinash.hw15.dto.BookInfoWithReaderDTO;
import dv.kinash.hw15.dto.BookNewDTO;
import dv.kinash.hw15.repository.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookService {
    public BookInfoDTO addGetNewBook(BookNewDTO newBook);
    public void deleteBook(Long bookId);
    public List<BookInfoDTO> getUserBook(String userEmail);
    public List<BookInfoDTO> getAvailableBook(String userEmail);
    public List<BookInfoWithReaderDTO> getAllBook();
    public void takeBook(String userEmail, Long bookId);
    public void returnBook(String userEmail, Long bookId);
}
