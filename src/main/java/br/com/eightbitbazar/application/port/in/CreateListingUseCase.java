package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.listing.CreateListingInput;
import br.com.eightbitbazar.application.usecase.listing.CreateListingOutput;
import br.com.eightbitbazar.domain.user.UserId;

public interface CreateListingUseCase {

    CreateListingOutput execute(UserId sellerId, CreateListingInput input);
}
