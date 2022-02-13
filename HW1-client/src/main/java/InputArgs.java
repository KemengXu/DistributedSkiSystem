public class InputArgs {

  public static final String NUM_THREADS = "--numThreads";
  public static final String NUM_SKIERS = "--numSkiers";
  public static final String NUM_LIFTS = "--numLifts";
  public static final String NUM_RUNS = "--numRuns";
  public static final String IP = "--ip";
  public static final String PORT = "--port";

  public int numThreads;
  public int numSkiers;
  public int numLifts;
  public int numRuns;
  public String ip;
  public String port;

  public InputArgs(int numThreads, int numSkiers, int numLifts, int numRuns, String ip, String port) {
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numLifts = numLifts;
    this.numRuns = numRuns;
    this.ip = ip;
    this.port = port;
  }

  public static InputArgs parseArgs(String[] args) {
    // --numThreads 100 --numSkiers 100 --numLifts 5 --numRuns 1 --ip localhost --port 8080
    int threadNum = 64;
    int skierNum = 1024;
    int liftNum = 40;
    int runNum = 10;
    String ip = null;
    String port = null;

    for (int i = 0; i < args.length; i+=2) {

      String curArg = args[i];
      String argValue = args[i+1];
      if (isSameString(NUM_THREADS, curArg)) {
        threadNum = Integer.parseInt(argValue);
      }
      if (isSameString(NUM_SKIERS, curArg)) {
        skierNum = Integer.parseInt(argValue);
      }
      if (isSameString(NUM_LIFTS, curArg)) {
        liftNum = Integer.parseInt(argValue);
      }
      if (isSameString(NUM_RUNS, curArg)) {
        runNum = Integer.parseInt(argValue);
      }
      if (isSameString(IP, curArg)) {
        ip = argValue;
      }
      if (isSameString(PORT, curArg)) {
        port = argValue;
      }
    }
    return new InputArgs(threadNum, skierNum, liftNum, runNum, ip, port);
  }


  private static boolean isSameString(String s1, String s2) {
    if (s1 == null) return s2 == null;
    return s1.equals(s2);
  }

}
