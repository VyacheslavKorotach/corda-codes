package net.corda.samples.tictacthor.contracts;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.samples.tictacthor.states.SopState;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class ContractTests {
    private final MockServices ledgerServices = new MockServices();
    TestIdentity Operator = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
    TestIdentity Operator2 = new TestIdentity(new CordaX500Name("Bob",  "TestLand",  "US"));

    @Test
    public void GameCanOnlyCreatedWhenTwoDifferentPlayerPresented() {
        UniqueIdentifier playerX = new UniqueIdentifier();
        SopState tokenPass = new SopState(new UniqueIdentifier(),new UniqueIdentifier(),
                new AnonymousParty(Operator.getPublicKey()),new AnonymousParty(Operator2.getPublicKey()));
        SopState tokenfail = new SopState(playerX,playerX,
                new AnonymousParty(Operator.getPublicKey()),new AnonymousParty(Operator2.getPublicKey()));
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(SopContract.ID, tokenfail);
                tx.command(Operator.getPublicKey(), new SopContract.Commands.StartSop()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.output(SopContract.ID, tokenPass);
                tx.command(Operator.getPublicKey(), new SopContract.Commands.StartSop()); // Wrong type.
                return tx.verifies();
            });
            return null;
        });
    }
}