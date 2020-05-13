package com.supplychain.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.sun.istack.NotNull;
import com.supplychain.accountUtilities.NewKeyForAccount;
import com.supplychain.contracts.InvoiceStateContract;
import com.supplychain.contracts.ShippingRequestStateContract;
import com.supplychain.states.InvoiceState;
import com.supplychain.states.PaymentState;
import com.supplychain.states.ShippingRequestState;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SendShippingRequest extends FlowLogic<String> {

    private final ProgressTracker progressTracker = tracker();

    private static final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating a HeartState transaction");
    private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with out private key.");
    private static final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Recording transaction") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.tracker();
        }
    };

    private static ProgressTracker tracker() {
        return new ProgressTracker(
                GENERATING_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
        );
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //private variables
    private String whoAmI ;
    private String whereTo;
    private Party shipper;
    private String cargo;


    //public constructor
    public SendShippingRequest(String whoAmI, String whereTo, Party shipper, String cargo){
        this.whoAmI = whoAmI;
        this.whereTo = whereTo;
        this.shipper = shipper;
        this.cargo = cargo;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        //grab account service
        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        //grab the account information
        AccountInfo myAccount = accountService.accountInfo(whoAmI).get(0).getState().getData();
        PublicKey myKey = subFlow(new NewKeyForAccount(myAccount.getIdentifier().getId())).getOwningKey();

        AccountInfo targetAccount = accountService.accountInfo(whereTo).get(0).getState().getData();

        //generating State for transfer
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        ShippingRequestState output = new ShippingRequestState(new AnonymousParty(myKey),whereTo,shipper,cargo);
        TransactionBuilder txbuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                .addOutputState(output)
                .addCommand(new ShippingRequestStateContract.Commands.Create(), Arrays.asList(shipper.getOwningKey(),myKey));

        //self sign Transaction
        SignedTransaction locallySignedTx = getServiceHub().signInitialTransaction(txbuilder,Arrays.asList(getOurIdentity().getOwningKey(),myKey));
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);

        //Collect sigs
        FlowSession sessionForAccountToSendTo = initiateFlow(shipper);
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo,shipper.getOwningKey()));
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);

        //Finalize
        subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));
        return "Request"+ shipper.nameOrNull() +" to send " + cargo+ " to "
                + targetAccount.getHost().nameOrNull().getOrganisation() + "'s "+ targetAccount.getName() + " team";
    }
}


@InitiatedBy(SendShippingRequest.class)
class SendShippingRequestResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public SendShippingRequestResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        subFlow(new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                // Custom Logic to validate transaction.
            }
        });
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}

