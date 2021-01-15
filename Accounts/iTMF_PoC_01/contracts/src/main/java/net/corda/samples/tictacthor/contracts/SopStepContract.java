package net.corda.samples.tictacthor.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.samples.tictacthor.states.SopStepState;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class SopStepContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.samples.tictacthor.contracts.SopContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<SopStepContract.Commands> command = requireSingleCommand(tx.getCommands(), SopStepContract.Commands.class);

        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();

        if (command.getValue() instanceof SopStepContract.Commands.StartSop) {

            // Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("No inputs should be consumed when creating a new Invoice State.", inputs.isEmpty());
                require.using("Transaction must have exactly one output.", outputs.size() == 1);
                SopStepState output = (SopStepState) outputs.get(0);
                require.using("Output sop must have status SOP_IN_PROGRESS", output.getStatus() == SopStepState.Status.SOP_IN_PROGRESS);
                require.using("You cannot do a SOP to yourself.", output.getParamedic() != output.getPatient());
                return null;
            });

        } else if (command.getValue() instanceof SopStepContract.Commands.SubmitSubStep){

        }else if (command.getValue() instanceof SopStepContract.Commands.EndSop){

        }else{
            throw new IllegalArgumentException("Command not found!");
        }

    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command
        class StartSop implements Commands {}
        class SubmitSubStep implements Commands {}
        class EndSop implements Commands {}
    }

    public static class SopUtils{
        public static Boolean isSopCompleted(char[][] sop){
            return (sop[0][0] == sop [0][1] && sop[0][0] == sop [0][2] && (sop[0][0] == 'X' || sop[0][0] == 'O')) ||
                    (sop[0][0] == sop [1][1] && sop[0][0] == sop [2][2]&& (sop[0][0] == 'X' || sop[0][0] == 'O')) ||
                    (sop[0][0] == sop [1][0] && sop[0][0] == sop [2][0]&& (sop[0][0] == 'X' || sop[0][0] == 'O')) ||
                    (sop[2][0] == sop [2][1] && sop[2][0] == sop [2][2]&& (sop[2][0] == 'X' || sop[2][0] == 'O')) ||
                    (sop[2][0] == sop [1][1] && sop[0][0] == sop [0][2]&& (sop[2][0] == 'X' || sop[2][0] == 'O')) ||
                    (sop[0][2] == sop [1][2] && sop[0][2] == sop [2][2]&& (sop[0][2] == 'X' || sop[0][2] == 'O')) ||
                    (sop[0][1] == sop [1][1] && sop[0][1] == sop [2][1]&& (sop[0][1] == 'X' || sop[0][1] == 'O')) ||
                    (sop[1][0] == sop [1][1] && sop[1][0] == sop [1][2]&& (sop[1][0] == 'X' || sop[1][0] == 'O'));
        }

    }
}
