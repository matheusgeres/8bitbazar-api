package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.listing.CreateListingInput;
import com.eightbitbazar.application.usecase.listing.CreateListingOutput;
import com.eightbitbazar.domain.user.UserId;

public interface CreateListingUseCase {

    CreateListingOutput execute(UserId sellerId, CreateListingInput input);
}
