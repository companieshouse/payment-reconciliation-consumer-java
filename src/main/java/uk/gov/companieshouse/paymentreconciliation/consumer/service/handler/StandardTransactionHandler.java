package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.util.List;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.EshuMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.PaymentTransactionsResourceDaoMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.EshuRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.TransactionRepository;

@Component
public class StandardTransactionHandler implements TransactionHandler<PaymentDetailsResponse, PaymentResponse> {
    private final EshuRepository eshuRepository;
    private final TransactionRepository transactionRepository;
    private final EshuMapper eshuMapper;
    private final PaymentTransactionsResourceDaoMapper paymentTransactionsResourceDaoMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public StandardTransactionHandler(EshuRepository eshuRepository, TransactionRepository transactionRepository,
                                     EshuMapper eshuMapper, PaymentTransactionsResourceDaoMapper paymentTransactionsResourceDaoMapper) {
        this.eshuRepository = eshuRepository;
        this.transactionRepository = transactionRepository;
        this.eshuMapper = eshuMapper;
        this.paymentTransactionsResourceDaoMapper = paymentTransactionsResourceDaoMapper;
    }

    @Override
    public void handle(PaymentDetailsResponse paymentDetails, PaymentResponse paymentResponse) {
        String paymentId = paymentResponse.getReference();
        LOGGER.info("Creating Eshu resources for payment id: %s ".formatted(paymentId));
        List<EshuDao> eshuResources = eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, paymentDetails.getTransactionDate());
        eshuRepository.saveAll(eshuResources);

        LOGGER.info("Creating PaymentTransactionsResourceDao resources for payment id: %s".formatted(paymentId));
        List<PaymentTransactionsResourceDao> paymentTransactionsResources = paymentTransactionsResourceDaoMapper.mapFromPaymentResponse(
                paymentResponse, paymentId, paymentDetails.getTransactionDate(), paymentDetails.getPaymentStatus());
        transactionRepository.saveAll(paymentTransactionsResources);
    }
}
