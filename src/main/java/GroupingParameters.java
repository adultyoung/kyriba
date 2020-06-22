import lombok.Getter;
import lombok.Setter;

import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class GroupingParameters {

    private String username;
    private ChronoUnit timeUnit;
}
