package net.corda.samples.tictacthor.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import com.sun.istack.NotNull;
import net.corda.samples.tictacthor.accountUtilities.NewKeyForAccount;
import net.corda.samples.tictacthor.contracts.SopContract;
import net.corda.samples.tictacthor.states.SopState;
//import javafx.util.Pair;
import kotlin.Pair;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SubmitTurnFlow extends FlowLogic<String> {

    private final ProgressTracker progressTracker = tracker();

    private static final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating a HeartState transaction");
    private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
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
    private UniqueIdentifier sopId;
    private int sop;

    //public constructor
    public SubmitTurnFlow(UniqueIdentifier sopId, String whoAmI, String whereTo, int sop){
        this.sopId = sopId;
        this.whoAmI = whoAmI;
        this.whereTo = whereTo;
        this.sop = sop;
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
        AnonymousParty targetAcctAnonymousParty = subFlow(new RequestKeyForAccount(targetAccount));

        //retrieve the game board
        QueryCriteria.LinearStateQueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria()
                .withUuid(Arrays.asList(sopId.getId())).withStatus(Vault.StateStatus.UNCONSUMED);
        StateAndRef<SopState> inputSopStateAndRef = getServiceHub().getVaultService().queryBy(SopState.class,queryCriteria).getStates().get(0);
        if(inputSopStateAndRef == null){
            throw new IllegalArgumentException("You are in another SOP");
        }
        SopState inputSopState = inputSopStateAndRef.getState().getData();

        //check turns
        if (!inputSopState.getCurrentPlayerParty().toString().equals(myAccount.getIdentifier().toString())){
            throw new IllegalArgumentException("It's not your turn! "+ inputSopState.getCurrentPlayerParty() + " my account: "+myAccount.getIdentifier());
        }

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        //generating State for transfer
        SopState outputSopState = inputSopState.returnNewSopAfterMove(sop,new AnonymousParty(myKey), targetAcctAnonymousParty);

        // Obtain a reference to a notary we wish to use.
        /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
         *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
         *
         *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
         */
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
        // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

        TransactionBuilder txbuilder = new TransactionBuilder(notary)
                .addInputState(inputSopStateAndRef)
                .addOutputState(outputSopState)
                .addCommand(new SopContract.Commands.SubmitTurn(),Arrays.asList(myKey,targetAcctAnonymousParty.getOwningKey()));

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        //self verify and sign Transaction
        txbuilder.verify(getServiceHub());
        SignedTransaction locallySignedTx = getServiceHub().signInitialTransaction(txbuilder,Arrays.asList(getOurIdentity().getOwningKey(),myKey));

        //Collect sigs
        FlowSession sessionForAccountToSendTo = initiateFlow(targetAccount.getHost());
        List<TransactionSignature> accountToMoveToSignature = (List<TransactionSignature>) subFlow(new CollectSignatureFlow(locallySignedTx,
                sessionForAccountToSendTo,targetAcctAnonymousParty.getOwningKey()));
        SignedTransaction signedByCounterParty = locallySignedTx.withAdditionalSignatures(accountToMoveToSignature);
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        //Finalize
        SignedTransaction stx = subFlow(new FinalityFlow(signedByCounterParty,
                Arrays.asList(sessionForAccountToSendTo).stream().filter(it -> it.getCounterparty() != getOurIdentity()).collect(Collectors.toList())));
        subFlow(new SyncSop(outputSopState.getLinearId().toString(),targetAccount.getHost()));
        return "rxId: "+stx.getId();
    }
}


@InitiatedBy(SubmitTurnFlow.class)
class SubmitTurnFlowResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public SubmitTurnFlowResponder(FlowSession counterpartySession) {
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

