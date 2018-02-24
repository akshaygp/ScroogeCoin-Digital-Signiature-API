import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class TxHandler {
	UTXOPool Ledger;
	    /**
	     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
	     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
	     * constructor.

	     */
	    public TxHandler(UTXOPool utxoPool) {
	       Ledger = utxoPool;
	    }

	    /**
	     * @return true if:
	     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
	     * (2) the signatures on each input of {@code tx} are valid,
	     * (3) no UTXO is claimed multiple times by {@code tx},
	     * (4) all of {@code tx}s output values are non-negative, and
	     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
	     *     values; and false otherwise.
	     */
	    public boolean isValidTx(Transaction SampleTx) {
	        double SampleInputValue = 0.0;							//Initializing Aggregated Input and Output Values
	        
	        ArrayList<Transaction.Input> InputTransactions = SampleTx.getInputs();
	        ArrayList<Transaction.Output> OutputTransactions = SampleTx.getOutputs();
	        double SampleOutputValue = 0.0;
	        for (int k = 0; k < OutputTransactions.size(); k++)
	        {
	            if (OutputTransactions.get(k).value < 0) {
	            	return false; 																	//Condition 4
	            }
	        SampleOutputValue += OutputTransactions.get(k).value; 
	        }
	        HashSet<UTXO> UTXOHash = new HashSet<UTXO>(); 
	        

	        for (int i = 0; i < InputTransactions.size(); i++) {
	            byte[] SampleMessage = SampleTx.getRawDataToSign(i);
	            Transaction.Input InputElement = InputTransactions.get(i);
	            byte[] SampleSignature = InputElement.signature;

	            UTXO SampleUTXO = new UTXO(InputElement.prevTxHash, InputElement.outputIndex);
	            if (!Ledger.contains(SampleUTXO) || UTXOHash.contains(SampleUTXO)) {				//Condition 3
	                return false;
	            }
	            if(!UTXOHash.contains(SampleUTXO)) {										//Updating the UTXO Pool
	            	UTXOHash.add(SampleUTXO);
	            }

	            Transaction.Output SampleUTXOOutput = Ledger.getTxOutput(SampleUTXO);		
	            if (SampleUTXOOutput == null) {						//No details for sample Txn in Output list of Ledger
	                return false;
	            }
	            SampleInputValue += SampleUTXOOutput.value;

	            //RSAKey SamplePK = (RSAKey) SampleUTXOOutput.address;
	            
	            RSAKey SampleRSAKey = SampleUTXOOutput.address;
	            if (!SampleRSAKey.verifySignature(SampleMessage, SampleSignature)) {				//Condition 2
	                return false;
	            }
	        }
	        if (SampleInputValue < SampleOutputValue) {
	            return false;																		//Condition 5
	        } 
	         else {
	            return true;
	        }
	    }

	    /**
	     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
	     * transaction for correctness, returning a mutually valid array of accepted transactions, and
	     * updating the current UTXO pool as appropriate.
	     */
	    public Transaction[] handleTxs(Transaction[] possibleTxs) {
	        ArrayList<Transaction> AllTransactions = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
	        ArrayList<Transaction> ValidTransactions = new ArrayList<Transaction>();
	        ArrayList<Transaction> InvalidTransactions = new ArrayList<Transaction>();
	        int SampleUTXOIndex = 0;
	        
	        
	        for(Transaction SampleTransaction : AllTransactions)
	        {
	            if(isValidTx(SampleTransaction)){						//Checking each transaction for correctness
	                ValidTransactions.add(SampleTransaction);
	                																		//Updating the UTXO Pool
	                for (Transaction.Input InputElement : SampleTransaction.getInputs()) {	
	                    UTXO SampleUTXORemove = new UTXO(InputElement.prevTxHash, InputElement.outputIndex);
	                    Ledger.removeUTXO(SampleUTXORemove);
	                }
	                
	                for (Transaction.Output SampleUTXOOutput : SampleTransaction.getOutputs()) {
	                    UTXO SampleUTXOAdd = new UTXO(SampleTransaction.getHash(), SampleUTXOIndex);
	                    Ledger.addUTXO(SampleUTXOAdd, SampleUTXOOutput);
	                    SampleUTXOIndex++;
	                }

	            }
	            else
	            {
	                InvalidTransactions.add(SampleTransaction);
	            }
	        }
            int ValidTransactionSize = ValidTransactions.size();
	        return ValidTransactions.toArray(new Transaction[ValidTransactionSize]);
	    }

	}

