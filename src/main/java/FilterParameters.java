import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FilterParameters {
    private String username;
    private LocalDateTime startOfPeriod;
    private LocalDateTime endOfPeriod;
    private String messagePattern;
}
