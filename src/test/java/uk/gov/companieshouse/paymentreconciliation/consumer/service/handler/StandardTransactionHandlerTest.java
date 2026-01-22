package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.EshuMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.PaymentTransactionsResourceDaoMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.EshuRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.TransactionRepository;



class StandardTransactionHandlerTest {

    private EshuRepository eshuRepository;
    private TransactionRepository transactionRepository;
    private EshuMapper eshuMapper;
    private PaymentTransactionsResourceDaoMapper paymentTransactionsResourceDaoMapper;
    private StandardTransactionHandler handler;

    @BeforeEach
    void setUp() {
        eshuRepository = mock(EshuRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        eshuMapper = mock(EshuMapper.class);
        paymentTransactionsResourceDaoMapper = mock(PaymentTransactionsResourceDaoMapper.class);
        handler = new StandardTransactionHandler(eshuRepository, transactionRepository, eshuMapper, paymentTransactionsResourceDaoMapper);
    }

    @Test
    void handle_shouldMapAndSaveEshuAndPaymentTransactionsResources() {
        // Arrange
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);
        PaymentResponse paymentResponse = mock(PaymentResponse.class);

        String paymentId = "PAY123";
        String transactionDate = LocalDateTime.now().toString();
        String paymentStatus = "PAID";

        when(paymentResponse.getReference()).thenReturn(paymentId);
        when(paymentDetails.getTransactionDate()).thenReturn(transactionDate);
        when(paymentDetails.getPaymentStatus()).thenReturn(paymentStatus);

        List<EshuDao> eshuList = Arrays.asList(mock(EshuDao.class), mock(EshuDao.class));
        List<PaymentTransactionsResourceDao> daoList = Arrays.asList(mock(PaymentTransactionsResourceDao.class));

        when(eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate)).thenReturn(eshuList);
        when(paymentTransactionsResourceDaoMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate, paymentStatus)).thenReturn(daoList);

        // Act
        handler.handle(paymentDetails, paymentResponse);

        // Assert
        verify(eshuMapper).mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);
        verify(eshuRepository).saveAll(eshuList);

        verify(paymentTransactionsResourceDaoMapper).mapFromPaymentResponse(paymentResponse, paymentId, transactionDate, paymentStatus);
        verify(transactionRepository).saveAll(daoList);
    }

    @Test
    void handle_shouldHandleEmptyListsGracefully() {
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);
        PaymentResponse paymentResponse = mock(PaymentResponse.class);

        String paymentId = "PAY456";
        String transactionDate = LocalDateTime.now().toString();
        String paymentStatus = "FAILED";

        when(paymentResponse.getReference()).thenReturn(paymentId);
        when(paymentDetails.getTransactionDate()).thenReturn(transactionDate);
        when(paymentDetails.getPaymentStatus()).thenReturn(paymentStatus);

        when(eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate)).thenReturn(List.of());
        when(paymentTransactionsResourceDaoMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate, paymentStatus)).thenReturn(List.of());

        handler.handle(paymentDetails, paymentResponse);

        verify(eshuRepository).saveAll(List.of());
        verify(transactionRepository).saveAll(List.of());
    }
}