import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SortingVisualizer extends JPanel {

    private int[] array;
    private final int size = 100;
    private int currentIndex = -1;
    private int comparingIndex = -1;
    private boolean isSorting = false;
    private volatile boolean stopRequested = false;
    private Thread sortingThread;

    private Timer timer;
    private long startTime;
    public JLabel timerLabel;

    public SortingVisualizer() {
        generateArray();
    }

    public void generateArray() {
        if (isSorting) return;
        array = new int[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = rand.nextInt(400) + 10;
        }
        repaint();
        stopTimer();
        if (timerLabel != null) timerLabel.setText("Elapsed Time: 0.00 sec");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth() / size;
        for (int i = 0; i < size; i++) {
            if (i == currentIndex)
                g.setColor(Color.RED);
            else if (i == comparingIndex)
                g.setColor(Color.YELLOW);
            else
                g.setColor(Color.CYAN);

            g.fillRect(i * width, getHeight() - array[i], width - 2, array[i]);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    private void startSort(Runnable algorithm) {
        stopRequested = false;
        isSorting = true;
        startTimer();
        sortingThread = new Thread(() -> {
            algorithm.run();
            resetHighlights();
        });
        sortingThread.start();
    }

    private void stopSort() {
        stopRequested = true;
        if (sortingThread != null && sortingThread.isAlive()) {
            sortingThread.interrupt();
        }
        resetHighlights();
    }

    private void resetHighlights() {
        isSorting = false;
        stopTimer();
        currentIndex = -1;
        comparingIndex = -1;
        repaint();
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        timer = new Timer(100, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (timerLabel != null) {
                timerLabel.setText(String.format("Elapsed Time: %.2f sec", elapsed / 1000.0));
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void swap(int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    // ------------------ Sorting Algorithms ------------------

    public void bubbleSort() {
        startSort(() -> {
            for (int i = 0; i < array.length - 1 && !stopRequested; i++) {
                for (int j = 0; j < array.length - i - 1 && !stopRequested; j++) {
                    currentIndex = j;
                    comparingIndex = j + 1;
                    if (array[j] > array[j + 1]) {
                        swap(j, j + 1);
                    }
                    repaint();
                    sleep(10);
                }
            }
        });
    }

    public void selectionSort() {
        startSort(() -> {
            for (int i = 0; i < array.length - 1 && !stopRequested; i++) {
                int minIdx = i;
                for (int j = i + 1; j < array.length && !stopRequested; j++) {
                    currentIndex = j;
                    comparingIndex = minIdx;
                    if (array[j] < array[minIdx]) {
                        minIdx = j;
                    }
                    repaint();
                    sleep(10);
                }
                swap(i, minIdx);
            }
        });
    }

    public void insertionSort() {
        startSort(() -> {
            for (int i = 1; i < array.length && !stopRequested; i++) {
                int key = array[i];
                int j = i - 1;
                while (j >= 0 && array[j] > key && !stopRequested) {
                    currentIndex = j;
                    comparingIndex = j + 1;
                    array[j + 1] = array[j];
                    j--;
                    repaint();
                    sleep(10);
                }
                array[j + 1] = key;
                repaint();
                sleep(10);
            }
        });
    }

    public void mergeSort() {
        startSort(() -> mergeSortHelper(0, array.length - 1));
    }

    private void mergeSortHelper(int left, int right) {
        if (left < right && !stopRequested) {
            int mid = (left + right) / 2;
            mergeSortHelper(left, mid);
            mergeSortHelper(mid + 1, right);
            merge(left, mid, right);
        }
    }

    private void merge(int left, int mid, int right) {
        if (stopRequested) return;

        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;

        while (i <= mid && j <= right && !stopRequested) {
            currentIndex = i;
            comparingIndex = j;
            repaint();
            sleep(10);

            if (array[i] <= array[j]) {
                temp[k++] = array[i++];
            } else {
                temp[k++] = array[j++];
            }
        }

        while (i <= mid && !stopRequested) {
            currentIndex = i;
            temp[k++] = array[i++];
            repaint();
            sleep(10);
        }

        while (j <= right && !stopRequested) {
            comparingIndex = j;
            temp[k++] = array[j++];
            repaint();
            sleep(10);
        }

        for (i = 0; i < temp.length && !stopRequested; i++) {
            array[left + i] = temp[i];
            repaint();
            sleep(10);
        }
    }

    public void quickSort() {
        startSort(() -> quickSortHelper(0, array.length - 1));
    }

    private void quickSortHelper(int low, int high) {
        if (low < high && !stopRequested) {
            int pi = partition(low, high);
            quickSortHelper(low, pi - 1);
            quickSortHelper(pi + 1, high);
        }
    }

    private int partition(int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high && !stopRequested; j++) {
            currentIndex = j;
            comparingIndex = high;
            repaint();
            sleep(10);
            if (array[j] < pivot) {
                i++;
                swap(i, j);
            }
        }
        swap(i + 1, high);
        return i + 1;
    }

    // ------------------ Main Method ------------------

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sorting Visualizer");
        SortingVisualizer visualizer = new SortingVisualizer();

        String[] algorithms = {
                "Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"
        };
        JComboBox<String> algoBox = new JComboBox<>(algorithms);

        JButton sortButton = new JButton("Start Sort");
        JButton stopButton = new JButton("Stop");
        JButton generateButton = new JButton("Generate New Array");

        JLabel timerLabel = new JLabel("Elapsed Time: 0.00 sec");
        visualizer.timerLabel = timerLabel;

        sortButton.addActionListener(e -> {
            if (visualizer.isSorting) return;
            String selected = (String) algoBox.getSelectedItem();
            switch (selected) {
                case "Bubble Sort" -> visualizer.bubbleSort();
                case "Selection Sort" -> visualizer.selectionSort();
                case "Insertion Sort" -> visualizer.insertionSort();
                case "Merge Sort" -> visualizer.mergeSort();
                case "Quick Sort" -> visualizer.quickSort();
            }
        });

        stopButton.addActionListener(e -> visualizer.stopSort());
        generateButton.addActionListener(e -> visualizer.generateArray());

        JPanel controlPanel = new JPanel();
        controlPanel.add(generateButton);
        controlPanel.add(algoBox);
        controlPanel.add(sortButton);
        controlPanel.add(stopButton);
        controlPanel.add(timerLabel);

        frame.setLayout(new BorderLayout());
        frame.add(visualizer, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
