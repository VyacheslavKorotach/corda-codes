package net.corda.samples.tictacthor.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.samples.tictacthor.states.SopState;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class SopContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.samples.tictacthor.contracts.SopContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<SopContract.Commands> command = requireSingleCommand(tx.getCommands(), SopContract.Commands.class);

        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();

        if (command.getValue() instanceof Commands.StartSop) {

            // Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("No inputs should be consumed when creating a new Invoice State.", inputs.isEmpty());
                require.using("Transaction must have exactly one output.", outputs.size() == 1);
                SopState output = (SopState) outputs.get(0);
                require.using("Output board must have status SOP_IN_PROGRESS", output.getStatus() == SopState.Status.SOP_IN_PROGRESS);
                require.using("You cannot do a SOP to yourself.", output.getParamedic() != output.getPatient());
                require.using("First Sub Step (sop) must be equal 0.", output.getSop() == 0);
                return null;
            });
        } else if (command.getValue() instanceof SopContract.Commands.SubmitTurn){
            requireThat(require -> {
                require.using("Transaction must have exactly one input.", inputs.size() == 1);
                require.using("Transaction must have exactly one output.", outputs.size() == 1);
                SopState input = (SopState) inputs.get(0);
                SopState output = (SopState) outputs.get(0);
                require.using("Input must have status SOP_IN_PROGRESS", input.getStatus() == SopState.Status.SOP_IN_PROGRESS);
                require.using("The range of possible SOP sub steps is (0 - 4)", output.getSop() >=0 && output.getSop() <= 4);
                require.using("Next Sub Step of SOP either must be increment of previous one or be the Cancel Step", output.getSop() == input.getSop() + 1 || output.getSop() == 0 || output.getSop() == 4);
                return null;
            });
        }else if (command.getValue() instanceof Commands.EndSop){
//            requireThat(require -> {
//                SopState input = (SopState) inputs.get(0);
//                SopState output = (SopState) outputs.get(0);
//                require.using("Input must have status SOP_COMPLETED", input.getStatus() == SopState.Status.SOP_COMPLETED);
//                require.using("Next Sub Step of SOP either must be increment of previous one or be the Cancel Step", input.getSop() == 3 || input.getSop() == 4);
//                require.using("Next Sub Step of SOP either must be increment of previous one or be the Cancel Step", input.getSop() == output.getSop());
//                return null;
//            });
        }else{
            throw new IllegalArgumentException("Command not found!");
        }

    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class StartSop implements Commands {}
        class SubmitTurn implements Commands {}
        class EndSop implements Commands {}
    }

    public static class SopUtils {
        public static Boolean isSOPOver(int sop){
            return sop == 3 || sop == 4;
        }

    }
}
