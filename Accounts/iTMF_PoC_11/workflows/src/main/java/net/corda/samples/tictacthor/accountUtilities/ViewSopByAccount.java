package net.corda.samples.tictacthor.accountUtilities;

import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.tictacthor.states.SopState;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@StartableByRPC
@StartableByService
public class ViewSopByAccount extends FlowLogic<List<String>>{

    private final String acctName;

    public ViewSopByAccount(String acctname) {
        this.acctName = acctname;
    }

    @Override
    public List<String> call() throws FlowException {

        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        AccountInfo myAccount = accountService.accountInfo(acctName).get(0).getState().getData();
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria()
                .withExternalIds(Arrays.asList(myAccount.getIdentifier().getId()));

        List<String> Paramedic = getServiceHub().getVaultService().queryBy(SopState.class,criteria).getStates().stream().map(
                it -> "\nSOP Paramedic : " + it.getState().getData().getParamedicName()).collect(Collectors.toList());

        List<String> Patient = getServiceHub().getVaultService().queryBy(SopState.class,criteria).getStates().stream().map(
                it -> "\nSOP Patient : " + it.getState().getData().getPatientName()).collect(Collectors.toList());

        List<String> SOPs = getServiceHub().getVaultService().queryBy(SopState.class,criteria).getStates().stream().map(
                it -> "\nSOP State : " + it.getState().getData().getSop()).collect(Collectors.toList());

        List<String> SopStepDescriptions = getServiceHub().getVaultService().queryBy(SopState.class,criteria).getStates().stream().map(
                it -> "\nSOP State : " + it.getState().getData().getSubStepDescription()).collect(Collectors.toList());

        List<String> Statuses = getServiceHub().getVaultService().queryBy(SopState.class,criteria).getStates().stream().map(
                it -> "\nSOP Status : " + it.getState().getData().getStatus()).collect(Collectors.toList());

        return Stream.of(Paramedic, Patient, SOPs, SopStepDescriptions, Statuses).flatMap(Collection::stream).collect(Collectors.toList());
    }
}