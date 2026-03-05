package de.muenchen.oss.ad2image.spring;

import de.muenchen.oss.ad2image.starter.core.AvatarGenerator;
import de.muenchen.oss.ad2image.starter.core.DirectoryLookupService;
import de.muenchen.oss.ad2image.starter.core.EwsUserPhotoService;
import de.muenchen.oss.ad2image.starter.core.Mode;
import de.muenchen.oss.ad2image.starter.spring.AvatarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AvatarServiceTest {

    @InjectMocks
    private AvatarService sut;

    @Mock
    AvatarGenerator avatarGenerator;

    @Mock
    DirectoryLookupService directoryLookupService;

    @Mock
    EwsUserPhotoService ewsUserPhotoService;

    @Test
    void test() {
        sut.get("firstname.lastname", Mode.M_FALLBACK_GENERIC, 64);
    }
}
