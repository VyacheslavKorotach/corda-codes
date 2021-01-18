package net.corda.samples.tictacthor.webserver;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.samples.tictacthor.accountUtilities.CreateNewAccount;
import net.corda.samples.tictacthor.accountUtilities.ShareAccountTo;
import net.corda.samples.tictacthor.accountUtilities.mySop;
import net.corda.samples.tictacthor.flows.EndSopFlow;
import net.corda.samples.tictacthor.flows.StartSopFlow;
import net.corda.samples.tictacthor.flows.SubmitTurnFlow;
import net.corda.samples.tictacthor.flows.SubmitTemperatureFlow;
import net.corda.samples.tictacthor.accountUtilities.ViewSopByAccount;
import net.corda.samples.tictacthor.states.SopState;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import java.util.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();

    }

    /** Helpers for filtering the network map cache. */
    public String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @PostMapping(value =  "createNewAccount/{acctName}")
    private ResponseEntity<String> createNewAccount(@PathVariable String acctName){
        try{
            String result = proxy.startTrackedFlowDynamic(CreateNewAccount.class,acctName).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("Account "+acctName+" Created");

        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "shareAccountTo/{whoAmI}/{counerParty}")
    private ResponseEntity<String> shareAccountTo(@PathVariable String whoAmI, @PathVariable String counerParty){
        Set<Party> matchingPasties = proxy.partiesFromName(counerParty,false);
        try{

            Iterator iter = matchingPasties.iterator();
            String result = proxy.startTrackedFlowDynamic(ShareAccountTo.class,whoAmI,iter.next()).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("Account was shared with "+whoAmI);

        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "startSopFlow/{whoAmI}/{counterParty}")
    private ResponseEntity<String> startSopFlow(@PathVariable String whoAmI,
                                                @PathVariable String counterParty){
        int sopSubStep = 0;
        try{
            UniqueIdentifier sopNum = proxy.startTrackedFlowDynamic(StartSopFlow.class,whoAmI, counterParty).getReturnValue().get();
            String submitTurn = proxy.startTrackedFlowDynamic(SubmitTurnFlow.class, sopNum, whoAmI, counterParty,sopSubStep).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("SOP Num Created: "+sopNum+", "+whoAmI+" set the 0 sub step of the SOP");

        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "SubmitTurnFlow/{whoAmI}/{counterParty}/{newSubStep}")
    private ResponseEntity<String> SubmitTurnFlow(@PathVariable String whoAmI,
                                                  @PathVariable String counterParty,
                                                  @PathVariable String newSubStep) {
        int sopSubStep = Integer.parseInt(newSubStep);
        try{
            UniqueIdentifier sopId = proxy.startTrackedFlowDynamic(mySop.class,whoAmI).getReturnValue().get().getLinearId();
            String submitTurn = proxy.startTrackedFlowDynamic(SubmitTurnFlow.class, sopId, whoAmI, counterParty,sopSubStep).getReturnValue().get();

            if(isSopCompleted(whoAmI)){
                proxy.startTrackedFlowDynamic(EndSopFlow.class, sopId, whoAmI, counterParty).getReturnValue().get();
                return ResponseEntity.status(HttpStatus.CREATED).body(""+whoAmI+" changed the SOP sub step to "+sopSubStep+" and SOP was completed");
            }else{
                return ResponseEntity.status(HttpStatus.CREATED).body(""+whoAmI+" changed the SOP sub step to "+sopSubStep);
            }
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "SubmitTemperatureFlow/{whoAmI}/{counterParty}/{temperatureValue}")
    private ResponseEntity<String> SubmitTemperatureFlow(@PathVariable String whoAmI,
                                                         @PathVariable String counterParty,
                                                         @PathVariable String temperatureValue) {
        int sopSubStep = 2;
        float tempVal = Float.parseFloat(temperatureValue);
        try{
            UniqueIdentifier sopId = proxy.startTrackedFlowDynamic(mySop.class,whoAmI).getReturnValue().get().getLinearId();
            String submitTurn = proxy.startTrackedFlowDynamic(SubmitTemperatureFlow.class, sopId, whoAmI, counterParty,sopSubStep, tempVal).getReturnValue().get();

            if(isSopCompleted(whoAmI)){
                proxy.startTrackedFlowDynamic(EndSopFlow.class, sopId, whoAmI, counterParty).getReturnValue().get();
                return ResponseEntity.status(HttpStatus.CREATED).body(""+whoAmI+" changed the SOP sub step to "+sopSubStep+" and SOP was completed");
            }else{
                return ResponseEntity.status(HttpStatus.CREATED).body(""+whoAmI+" changed the SOP sub step to "+sopSubStep);
            }
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "viewSopByAccount/{whoAmI}")
    private ResponseEntity<String> viewSopByAccount(@PathVariable String whoAmI){
        try{
            List<String> sopInfo = proxy.startTrackedFlowDynamic(ViewSopByAccount.class,whoAmI).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("SOP Info: "+sopInfo);

        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    private Boolean isSopCompleted(String whoAmI){
        //If the SOP is over, the Status should be a null variable
        //So if the status returned is SOP_IN_PROGRESS, it means the SOP is not over.
        try{
            SopState.Status sopStatus = proxy.startTrackedFlowDynamic(mySop.class,whoAmI).getReturnValue().get().getStatus();
            return sopStatus != SopState.Status.SOP_IN_PROGRESS;
        }catch (Exception e) {
            throw new IllegalArgumentException("No SOP");
        }
    }
}