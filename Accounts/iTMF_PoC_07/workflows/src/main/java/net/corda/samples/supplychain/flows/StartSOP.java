package net.corda.samples.supplychain.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.sun.istack.NotNull;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.supplychain.accountUtilities.NewKeyForAccount;
//import net.corda.samples.supplychain.contracts.SOPStateContract;
import net.corda.samples.supplychain.contracts.SOPStateContract;
//import net.corda.samples.supplychain.states.SOPState;
import net.corda.samples.supplychain.states.SOPState;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class StartSOP extends FlowLogic<String> {

    //private variables
    private String pickupFrom ;
    private String whereTo;
    private String cargo;


    //public constructor
    public StartSOP(String pickupFrom, String shipTo, String cargo){
        this.pickupFrom = pickupFrom;
        this.whereTo = shipTo;
        this.cargo = cargo;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        //grab account service
        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        //grab the account information
        AccountInfo myAccount = accountService.accountInfo(pickupFrom).get(0).getState().getData();
        PublicKey myKey = subFlow(new NewKeyForAccount(myAccount.getIdentifier().getId())).getOwningKey();

//        AnonymousParty sellerAnonymousParty = subFlow(new RequestKeyForAccount(myAccount));

        AccountInfo targetAccount = accountService.accountInfo(whereTo).get(0).getState().getData();
        AnonymousParty targetAcctAnonymousParty = subFlow(new RequestKeyForAccount(targetAccount));

        //generating State for transfer
        SOPState output = new SOPState(new AnonymousParty(myKey),targetAcctAnonymousParty,cargo,getOurIdentity());

        // Obtain a reference to a notary we wish to use.
        /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
         *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
         *
         *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
         */
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
        // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

//        TransactionBuilder txbuilder = new TransactionBuilder(notary)
//                .addOutputState(output)
//                .addCommand(new SOPStateContract.Commands.Create(), Arrays.asList(targetAcctAnonymousParty.getOwningKey(),getOurIdentity().getOwningKey()));

        TransactionBuilder txbuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new SOPStateContract.Commands.Create(), Arrays.asList(targetAcctAnonymousParty.getOwningKey(),getOurIdentity().getOwningKey()));


        //self sign Transaction
        SignedTransaction locallySignedTx = getServiceHub().signInitialTransaction(txbuilder,Arrays.asList(getOurIdentity().getOwningKey()));

        //Collect sigs
        FlowSession sessionForAccountToSendTo = initiateFlow(targetAccount.getHost());
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo,targetAcctAnonymousParty.getOwningKey()));
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);

        //Finalize

//        List<FlowSession> sessions = Arrays.asList(initiateFlow(targetAccount.getHost()), initiateFlow(myAccount.getHost()));
        // We distribute the transaction to both the buyer and the state regulator using `FinalityFlow`.

//        subFlow(new FinalityFlow(signedByCounterParty, sessions));

        subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));

//        List<FlowSession> sessions = Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList());
//        sessions.add(initiateFlow(myAccount.getHost()));
//        subFlow(new FinalityFlow(signedByCounterParty,sessions));


        // We also distribute the transaction to the national regulator manually.
        subFlow(new ReportManually(signedByCounterParty, myAccount.getHost()));

        return "send " + cargo+ " to " + targetAccount.getHost().getName().getOrganisation() + "'s "+ targetAccount.getName() + " team";

    }
}


@InitiatedBy(StartSOP.class)
class StartSOPResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public StartSOPResponder(FlowSession counterpartySession) {
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

