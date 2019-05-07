import currency_pb2 as pb2
import currency_pb2_grpc as pb2_grpc
import grpc
import concurrent.futures as futures
import time
from random import randint
from random import uniform


class ExchangeServiceServicer(pb2_grpc.ExchangeServiceServicer):
    def __init__(self):
        self.rates = {pb2.PLN: 1.0, pb2.USD: 3.82, pb2.EUR: 4.28, pb2.GBP: 5.01}

    def ActualRates(self, request, context):
        response = pb2.Response()
        for i, currency in enumerate(request.currencies):
            rate = pb2.Rate()
            rate.currency = currency
            rate.rate = self.rates[currency]
            response.rates.extend([rate])
        return response

    def RatesStream(self, request, context):
        while True:
            currency = randint(0, 3)
            change = uniform(0.8, 1.2)
            self.rates[currency] = self.rates[currency] * change
            if currency in request.currencies:
                time.sleep(2)
                rate = pb2.Rate()
                rate.currency = currency
                rate.rate = self.rates[currency]
                yield rate


def serve():

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_ExchangeServiceServicer_to_server(ExchangeServiceServicer(), server)
    server.add_insecure_port('[::]:9997')
    server.start()
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)


serve()
