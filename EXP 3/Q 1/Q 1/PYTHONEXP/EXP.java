import java.util.*;

public class j {
    public static void main(String[] args) {
        // Initialize an array to store the number of factors for each number
        int[] numWays = new int[1 << 19];
        
        // Calculate the number of factors for each number and store in numWays array
        for (int i = 1; i < numWays.length; i++)
            numWays[i] = numFactors(i);
        
        // Multiply the numWays array with itself
        int[] result = multiply(numWays, numWays);
        
        // Create an IntTree data structure
        IntTree myTree = new IntTree(1, 500001);
        
        // Populate the IntTree with values
        for (int i = 1; i <= 5e5; i++)
            myTree.change(i, i, (((long) result[i]) << 32) + (500001 - i));
        
        // Read input from the user
        Scanner stdin = new Scanner(System.in);
        int n = stdin.nextInt();
        for (int i = 0; i < n; i++) {
            int low = stdin.nextInt(), high = stdin.nextInt();
            long res = myTree.query(low, high);
            // Output the result
            System.out.println((500001 - (res & ((1L << 32) - 1))) + " " + (res >> 32));
        }
    }

    // Function to multiply two arrays
    public static int[] multiply(int[] a, int[] b) {
        if (a.length < 64) {
            // If array length is small, perform simple multiplication
            int[] res = new int[a.length << 1];
            for (int i = 0; i < a.length; i++)
                for (int j = 0; j < b.length; j++)
                    res[i + j] += (a[i] * b[j]);
            return res;
        }
        // If array length is large, use divide and conquer approach
        int[] aL = Arrays.copyOf(a, a.length / 2), bL = Arrays.copyOf(b, b.length / 2);
        int[] aH = Arrays.copyOfRange(a, a.length / 2, a.length), bH = Arrays.copyOfRange(b, b.length / 2, b.length);
        int[] lR = multiply(aL, bL), hR = multiply(aH, bH);
        int[] aS = add(aH, aL), bS = add(bH, bL);
        int[] mR = multiply(aS, bS);
        subtract(mR, lR, hR);
        int[] res = new int[a.length << 1];
        addIn(res, lR, 0);
        addIn(res, hR, a.length);
        addIn(res, mR, a.length / 2);
        return res;
    }

    // Function to add values of two arrays
    public static void addIn(int[] total, int[] vals, int shift) {
        for (int i = shift; i < shift + vals.length; i++)
            total[i] += vals[i - shift];
    }

    // Function to add two arrays
    public static int[] add(int[] a, int[] b) {
        int[] res = new int[a.length];
        for (int i = 0; i < res.length; i++)
            res[i] = a[i] + b[i];
        return res;
    }

    // Function to subtract values of two arrays
    public static void subtract(int[] a, int[] b, int[] c) {
        for (int i = 0; i < a.length; i++)
            a[i] -= (b[i] + c[i]);
    }

    // Function to calculate the number of factors of a number
    public static int numFactors(int n) {
        ArrayList<Factor> list = new ArrayList<>();
        int div = 2;
        while (div * div <= n) {
            int e = 0;
            while (n % div == 0) {
                e++;
                n /= div;
            }
            if (e > 0)
                list.add(new Factor(div, e));
            div++;
        }
        if (n > 1)
            list.add(new Factor(n, 1));
        int res = 1;
        for (Factor f : list)
            res *= (f.exp + 1);
        return res;
    }
}

// Class to represent factors
class Factor {
    public int b, exp;
    public Factor(int b, int e) {
        this.b = b;
        exp = e;
    }
}

// Class to represent a segment tree for range queries and updates
class IntTree {
    public int low, high;
    public long delta, value;
    public IntTree left, right;

    // Constructor
    public IntTree(int low, int high) {
        this.low = low;
        this.high = high;
        delta = value = 0;
        if (low != high) {
            int mid = (low + high) / 2;
            left = new IntTree(low, mid);
            right = new IntTree(mid + 1, high);
        }
    }

    // Propagate changes down the tree
    public void prop() {
        if (left != null) {
            left.delta += delta;
            right.delta += delta;
            delta = 0;
        } else {
            value += delta;
            delta = 0;
        }
    }

    // Update the value of the node
    public void update() {
        if (left != null)
            value = Math.max(left.value + left.delta, right.value + right.delta);
    }

    // Change values in a range
    public void change(int start, int end, long extra) {
        if (high < start || end < low)
            return;
        prop();
        if (start <= low && high <= end) {
            delta += extra;
            update();
            return;
        }
        left.change(start, end, extra);
        right.change(start, end, extra);
        update();
    }

    // Query for maximum value in a range
    public long query(int start, int end) {
        if (high < start || end < low)
            return 0L;
        if (start <= low && high <= end)
            return value + delta;
        prop();
        long l = left.query(start, end);
        long r = right.query(start, end);
        update();
        return Math.max(l, r);
    }
}
