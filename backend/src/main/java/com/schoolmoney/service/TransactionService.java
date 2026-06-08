package com.schoolmoney.service;

import com.schoolmoney.dto.DepositRequest;
import com.schoolmoney.dto.PaymentRequest;
import com.schoolmoney.dto.WithdrawalRequest;
import com.schoolmoney.model.Child;
import com.schoolmoney.model.Fundraiser;
import com.schoolmoney.model.Transaction;
import com.schoolmoney.model.User;
import com.schoolmoney.model.enums.FundraiserStatus;
import com.schoolmoney.model.enums.TransactionType;
import com.schoolmoney.repository.ChildRepository;
import com.schoolmoney.repository.FundraiserRepository;
import com.schoolmoney.repository.SchoolClassRepository;
import com.schoolmoney.repository.TransactionRepository;
import com.schoolmoney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FundraiserRepository fundraiserRepository;
    private final ChildRepository childRepository;
    private final SchoolClassRepository schoolClassRepository;

    @Transactional
    public User depositFunds(String userId, DepositRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBalance(user.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .toAccountNumber(user.getVirtualAccountNumber())
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .payerId(user.getId())
                .build();

        transactionRepository.save(transaction);
        return userRepository.save(user);
    }

    @Transactional
    public Transaction payForChild(String userId, PaymentRequest request) {
        User parent = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found"));
        Fundraiser fundraiser = fundraiserRepository.findById(request.getFundraiserId())
                .orElseThrow(() -> new RuntimeException("Fundraiser not found"));

        if (!child.getParentId().equals(parent.getId())) {
            throw new RuntimeException("Not your child");
        }
        if (!child.getClassId().equals(fundraiser.getClassId())) {
            throw new RuntimeException("Child is not in this class");
        }
        if (fundraiser.getStatus() == FundraiserStatus.CLOSED) {
            throw new RuntimeException("Fundraiser is closed");
        }
        if (parent.getBalance().compareTo(fundraiser.getAmountPerChild()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        parent.setBalance(parent.getBalance().subtract(fundraiser.getAmountPerChild()));
        fundraiser.setBalance(fundraiser.getBalance().add(fundraiser.getAmountPerChild()));

        userRepository.save(parent);
        fundraiserRepository.save(fundraiser);

        Transaction transaction = Transaction.builder()
                .fromAccountNumber(parent.getVirtualAccountNumber())
                .toAccountNumber(fundraiser.getVirtualAccountNumber())
                .amount(fundraiser.getAmountPerChild())
                .type(TransactionType.PAYMENT_FOR_CHILD)
                .fundraiserId(fundraiser.getId())
                .classId(fundraiser.getClassId())
                .childId(child.getId())
                .payerId(parent.getId())
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdrawFromFundraiser(String userId, WithdrawalRequest request) {
        Fundraiser fundraiser = fundraiserRepository.findById(request.getFundraiserId())
                .orElseThrow(() -> new RuntimeException("Fundraiser not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!fundraiser.getCreatorId().equals(userId)) {
            throw new RuntimeException("Only creator can withdraw");
        }
        if (fundraiser.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient fundraiser funds");
        }

        fundraiser.setBalance(fundraiser.getBalance().subtract(request.getAmount()));
        fundraiserRepository.save(fundraiser);

        String toAccount = request.isExternal() ? "EXTERNAL_BANK" : user.getVirtualAccountNumber();
        TransactionType type = request.isExternal() ? TransactionType.EXTERNAL_WITHDRAWAL : TransactionType.WITHDRAWAL;

        if (!request.isExternal()) {
            user.setBalance(user.getBalance().add(request.getAmount()));
            userRepository.save(user);
        }

        Transaction transaction = Transaction.builder()
                .fromAccountNumber(fundraiser.getVirtualAccountNumber())
                .toAccountNumber(toAccount)
                .amount(request.getAmount())
                .type(type)
                .fundraiserId(fundraiser.getId())
                .classId(fundraiser.getClassId())
                .payerId(user.getId())
                .build();

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getFundraiserTransactions(String fundraiserId) {
        fundraiserRepository.findById(fundraiserId).orElseThrow(() -> new RuntimeException("Fundraiser not found"));
        return transactionRepository.findByFundraiserId(fundraiserId);
    }

    public List<Transaction> getClassTransactions(String classId) {
        schoolClassRepository.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        return transactionRepository.findByClassId(classId);
    }
}