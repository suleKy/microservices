package com.myapp.accounts.service.impl;

import com.myapp.accounts.dto.AccountsDto;
import com.myapp.accounts.dto.CardsDto;
import com.myapp.accounts.dto.CustomerDetailsDto;
import com.myapp.accounts.dto.LoansDto;
import com.myapp.accounts.entity.Accounts;
import com.myapp.accounts.entity.Customer;
import com.myapp.accounts.exception.ResourceNotFoundException;
import com.myapp.accounts.mapper.AccountsMapper;
import com.myapp.accounts.mapper.CustomerMapper;
import com.myapp.accounts.repository.AccountsRepository;
import com.myapp.accounts.repository.CustomerRepository;
import com.myapp.accounts.service.ICustomersService;
import com.myapp.accounts.service.client.CardsFeignClient;
import com.myapp.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;
    /**
     *
     * @param mobileNumber
     * @return Customer details based on a given mobile number
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;
    }
}
