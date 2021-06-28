import sys

def compute(path1, path2):
    TS_total_time = [int(time.strip()) for time in open(path1, "r")]
    TJ_total_time = [int(time.strip()) for time in open(path2, "r")]
    TS_average = sum(TS_total_time) / len(TS_total_time)
    print();
    print('Average TS time in', path1, 'is: ', TS_average / 10 ** 6, ' ms')
    TJ_average = sum(TJ_total_time) / len(TJ_total_time)
    print('Average TJ time in', path2, 'is: ', TJ_average / 10 ** 6, ' ms')
    print('TS / TJ is: ', TS_average / TJ_average)
    print();
    

# python3 log_processing.py TS1.txt, TJ1.txt

compute(sys.argv[1], sys.argv[2]);
